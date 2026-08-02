package movieapp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean matches(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

} // end of class