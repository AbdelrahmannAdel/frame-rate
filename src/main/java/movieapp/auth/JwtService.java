package movieapp.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Instant;

public class JwtService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String SECRET = dotenv.get("JWT_SECRET");
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generateToken(int userId) {
        Instant expiresAt = Instant.now().plusSeconds(24 * 60 * 60); // 24 hours from now

        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(expiresAt)
                .sign(ALGORITHM);
    }

    public static DecodedJWT verifyToken(String token) {
        return JWT.require(ALGORITHM)
                .build()
                .verify(token);
    }

} // end of class