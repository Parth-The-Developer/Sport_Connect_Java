import model.GameSession;
import model.Player;

import java.util.List;
import java.util.Scanner;

/** Console prompts and formatting used by {@link Main}. */
public final class ConsoleUi {

    private final Scanner in;

    public ConsoleUi(Scanner in) {
        this.in = in;
    }

    public void printBanner() {
        System.out.println();
        System.out.println("  ╔" + "═".repeat(30) + "╗");
        System.out.println("  ║      SportConnect  v1.0      ║");
        System.out.println("  ╚" + "═".repeat(30) + "╝");
        System.out.println();
    }

    public void printHeader(String title) {
        System.out.println();
        System.out.println("  ── " + title + " " + "─".repeat(Math.max(0, 34 - title.length())));
        System.out.println();
    }

    public void printPlayerList(List<Player> list) {
        if (list.isEmpty()) {
            System.out.println("  No players found.");
        } else {
            System.out.printf("  %-4s  %-20s  %-12s  %-14s  %-12s  %s%n",
                "ID", "Name", "Sport", "Skill", "City", "Rating");
            System.out.println("  " + "─".repeat(74));
            for (Player p : list) {
                System.out.printf("  %-4d  %-20s  %-12s  %-14s  %-12s  %.1f  %s%n",
                    p.getPlayerId(),
                    cut(p.getName(),        20),
                    cut(p.getSport(),       12),
                    cut(p.getSkill_level(), 14),
                    cut(p.getCity(),        12),
                    p.getRating(),
                    p.isActive() ? "" : "[inactive]");
            }
            System.out.println("\n  " + list.size() + " player(s) found.");
        }
        pause();
    }

    public void ok(String msg)   { System.out.println("\n  [OK] " + msg + "\n"); }
    public void warn(String msg) { System.out.println("\n  [!]  " + msg + "\n"); }
    public void stub(String msg) { warn("Not yet integrated: " + msg); }

    public void pause() {
        System.out.print("\n  Press Enter to continue...");
        in.nextLine();
    }

    public String formatStatus(GameSession.SessionStatus status) {
        return switch (status) {
            case SCHEDULED -> "  SCHEDULED";
            case COMPLETED -> "  COMPLETED";
            case CANCELLED -> "  CANCELLED";
        };
    }

    public int readInt() {
        try {
            String line = in.nextLine().trim();
            if (line.isEmpty()) return -1;
            return Integer.parseInt(line);
        } catch (Exception e) {
            return -1;
        }
    }

    public int readIntInline() {
        try { return Integer.parseInt(in.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    public boolean adminEmpty(List<?> list, String message) {
        if (list == null || list.isEmpty()) {
            warn(message);
            return true;
        }
        return false;
    }

    private static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
