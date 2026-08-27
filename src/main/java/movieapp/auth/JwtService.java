package movieapp.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtService {

    private final Algorithm algorithm;

    public JwtService(@Value("${JWT_SECRET}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String generateToken(int userId) {
        Instant expiresAt = Instant.now().plusSeconds(30L * 24 * 60 * 60); // 30 days from now

        return JWT.create()
                .withClaim("userId", userId)
                .withExpiresAt(expiresAt)
                .sign(algorithm);

    } // end of generateToken()

    public DecodedJWT verifyToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token);
    } // end of verifyToken()

} // end of class