package service;

import model.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Saves / loads {@code data/player_signups.txt}.
 * <p><b>Flow:</b> (1) Build one readable row: {@code v4} + tabs + fields. (2) Encrypt with
 * {@link StorageCrypto} → file line {@code ENC1:...}. (3) On startup, decrypt and parse.
 * <p>Old rows without {@code ENC1:} (comma + Base64 fields) still load — legacy format.
 */
public class SignupPersistenceService {

    private static final Path FILE = Paths.get("data", "player_signups.txt");

    public void loadInto(AuthService auth, PlayerService players) {
        try {
            touchFile();
            if (!Files.exists(FILE) || Files.size(FILE) == 0) return;
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank()) continue;
                try {
                    Player p = parseAnyLine(line);
                    if (p == null || blank(p.getEmail()) || auth.playerExists(p.getEmail())) continue;
                    auth.restoreRegisteredPlayer(p);
                    players.restorePersistedPlayer(p);
                } catch (Exception ignored) { /* skip bad line */ }
            }
        } catch (IOException e) {
            System.out.println("[SignupStore] load error: " + e.getMessage());
        }
    }

    public void appendSignup(Player p) {
        if (p == null || p.getEmail() == null) return;
        try {
            touchFile();
            String row = encodeRowV4(p);           // readable: v4<TAB>email<TAB>...
            String line = StorageCrypto.encryptUtf8(row);
            Files.writeString(FILE, line + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("[SignupStore] save error: " + e.getMessage());
        }
    }

    /** Admin menu: one human-readable line per stored row. */
    public List<String> listDecryptedRecordsForAdmin() {
        List<String> out = new ArrayList<>();
        try {
            touchFile();
            if (!Files.exists(FILE) || Files.size(FILE) == 0) return out;
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                if (line == null || line.isBlank()) continue;
                String plain = toReadablePlain(line);
                if (plain == null) {
                    out.add("(cannot decrypt — wrong SPORTCONNECT_STORAGE_KEY or bad line)");
                } else {
                    out.add(formatForAdmin(plain));
                }
            }
        } catch (IOException e) {
            out.add("Error: " + e.getMessage());
        }
        return out;
    }

    // --- read one line from file → Player ---

    private static Player parseAnyLine(String line) {
        if (line.startsWith("ENC1:")) {
            String plain = StorageCrypto.decryptUtf8(line);
            return plain == null ? null : playerFromPlain(plain);
        }
        // legacy: comma-separated Base64 (no ENC1)
        if (line.contains(",") && line.split(",", -1).length >= 13) {
            return legacyCommaBase64ToPlayer(line);
        }
        return null;
    }

    private static String toReadablePlain(String line) {
        if (line.startsWith("ENC1:")) return StorageCrypto.decryptUtf8(line);
        if (line.contains(",") && line.split(",", -1).length >= 13) {
            Player p = legacyCommaBase64ToPlayer(line);
            return p == null ? null : encodeRowV4(p);
        }
        return null;
    }

    /** v4 = tab-separated; v3 = old pipe-separated with \\| escapes (still inside old ENC1 blobs). */
    private static Player playerFromPlain(String plain) {
        if (plain.startsWith("v4\t")) {
            String[] a = plain.split("\t", -1);
            return a.length >= 14 ? fillPlayer(a) : null;
        }
        if (plain.startsWith("v3|")) {
            List<String> a = splitV3Pipes(plain);
            return a.size() >= 14 ? fillPlayer(a.toArray(new String[0])) : null;
        }
        return null;
    }

    private static Player fillPlayer(String[] a) {
        // a[0]=version, a[1]=email ... a[13]=updated
        Player p = new Player();
        p.setEmail(emptyToNull(a[1]));
        p.setPasswordHash(emptyToNull(a[2]));
        p.setName(emptyToNull(a[3]));
        p.setPhone(emptyToNull(a[4]));
        p.setSport(emptyToNull(a[5]));
        p.setCity(emptyToNull(a[6]));
        p.setSkill_level(emptyToNull(a[7]));
        p.setAge(parseInt(a[8], 0));
        p.setExperience(parseInt(a[9], 0));
        p.setPlayerId(parseLong(a[10]));
        p.setActive(parseBool(a[11]));
        if (a.length > 12 && !blank(a[12])) p.setCreatedAt(LocalDateTime.parse(unescV3(a[12])));
        if (a.length > 13 && !blank(a[13])) p.setUpdatedAt(LocalDateTime.parse(unescV3(a[13])));
        return p;
    }

    private static String encodeRowV4(Player p) {
        String[] a = new String[] {
            "v4",
            nz(p.getEmail()),
            nz(p.getPasswordHash()),
            nz(p.getName()),
            nz(p.getPhone()),
            nz(p.getSport()),
            nz(p.getCity()),
            nz(p.getSkill_level()),
            String.valueOf(p.getAge()),
            String.valueOf(p.getExperience()),
            String.valueOf(p.getPlayerId()),
            String.valueOf(p.isActive()),
            p.getCreatedAt() != null ? p.getCreatedAt().toString() : "",
            p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : ""
        };
        for (int i = 0; i < a.length; i++) {
            a[i] = a[i].replace("\t", " "); // tabs forbidden inside fields
        }
        return String.join("\t", a);
    }

    private static String formatForAdmin(String plain) {
        String[] a = plain.startsWith("v4\t") ? plain.split("\t", -1) : splitV3Pipes(plain).toArray(new String[0]);
        if (a.length < 14) return plain;
        return String.format(
            "email=%s | id=%s | name=%s | phone=%s | sport=%s | city=%s | skill=%s | age=%s | exp=%s | active=%s | credential=%s | created=%s | updated=%s",
            a[1], a[10], a[3], a[4], a[5], a[6], a[7], a[8], a[9], a[11], a[2], a[12], a[13]);
    }

    // --- v3 pipe format (only for decrypting old saved rows) ---

    private static List<String> splitV3Pipes(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == '\\' || n == '|') {
                    cur.append(n);
                    i++;
                    continue;
                }
            }
            if (c == '|') {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }

    private static String unescV3(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == '\\' || n == '|') {
                    b.append(n);
                    i++;
                    continue;
                }
            }
            b.append(c);
        }
        return b.toString();
    }

    // --- legacy comma + Base64 ---

    private static Player legacyCommaBase64ToPlayer(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 13) return null;
        Player p = new Player();
        p.setEmail(b64d(parts[0]));
        p.setPasswordHash(emptyToNull(b64d(parts[1])));
        p.setName(emptyToNull(b64d(parts[2])));
        p.setPhone(emptyToNull(b64d(parts[3])));
        p.setSport(emptyToNull(b64d(parts[4])));
        p.setCity(emptyToNull(b64d(parts[5])));
        p.setSkill_level(emptyToNull(b64d(parts[6])));
        p.setAge(parseInt(b64d(parts[7]), 0));
        p.setExperience(parseInt(b64d(parts[8]), 0));
        p.setPlayerId(parseLong(b64d(parts[9])));
        p.setActive(parseBool(b64d(parts[10])));
        if (!blank(b64d(parts[11]))) p.setCreatedAt(LocalDateTime.parse(b64d(parts[11])));
        if (!blank(b64d(parts[12]))) p.setUpdatedAt(LocalDateTime.parse(b64d(parts[12])));
        return p;
    }

    private static String b64d(String s) {
        try {
            return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static void touchFile() throws IOException {
        Path parent = FILE.getParent();
        if (parent != null) Files.createDirectories(parent);
        if (!Files.exists(FILE)) Files.createFile(FILE);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static String emptyToNull(String s) {
        return blank(s) ? null : s;
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static Long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean parseBool(String s) {
        if (s == null) return true;
        return Boolean.parseBoolean(s.trim().toLowerCase(Locale.ROOT)) || "1".equals(s.trim());
    }
}
