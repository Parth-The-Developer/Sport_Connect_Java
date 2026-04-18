import model.Player;
import service.AuthService;
import service.PlayerService;

/** Loads demo admins and players for local runs. */
public final class SeedData {

    private SeedData() {}

    public static void seed(AuthService authService, PlayerService playerService) {
        authService.registerAdmin("admin", "admin123",
                                  "admin@sportconnect.com", "System Admin");
        authService.registerSuperAdmin("superadmin", "super123",
                                       "super@sportconnect.com", "Super Admin");
        addDemo(authService, playerService, "Lien Tran",      "lien@demo.com",      "647-111-2222", "Cricket",    "BEGINNER",     "Toronto",     21, 2);
        addDemo(authService, playerService, "Brian Carter",   "brian@demo.com",     "905-222-3333", "Cricket",    "ADVANCED",     "Brampton",    24, 5);
        addDemo(authService, playerService, "Shahshree Das",  "shahshree@demo.com", "416-333-4444", "Cricket",    "INTERMEDIATE", "Mississauga", 22, 3);
        addDemo(authService, playerService, "Hassana Diallo", "hassana@demo.com",   "647-444-5555", "Volleyball", "BEGINNER",     "Toronto",     20, 1);
        addDemo(authService, playerService, "Pooja Mehta",    "pooja@demo.com",     "905-555-6666", "Cricket",    "ADVANCED",     "Brampton",    26, 6);
        addDemo(authService, playerService, "Riddhi Shah",    "riddhi@demo.com",    "416-666-7777", "Soccer",     "INTERMEDIATE", "Mississauga", 23, 4);
    }

    private static void addDemo(AuthService authService, PlayerService playerService,
                               String name, String email, String phone,
                               String sport, String skill, String city, int age, int exp) {
        Player p = authService.registerPlayer(email, name, phone, sport);
        p.setPasswordHash("demo123");
        p.setSkill_level(skill);
        p.setCity(city);
        p.setAge(age);
        p.setExperience(exp);
        playerService.addPlayer(p);
    }
}
