package com.vovamisjul.dserver.web.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.dao.UserDao;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Component
public class JwtUserProvider {

    @Autowired
    public DeviceDao deviceDao;

    @Autowired
    public UserDao userDao;

    @Value("${jwt.password}")
    private String jwtSecret;
    @Value("${jwt.accessExpirationTimeSec}")
    private long jwtAccessExpirationTimeSec;
    @Value("${jwt.refreshExpirationTimeSec}")
    private long jwtRefreshExpirationTimeSec;

    public String generateAccessToken(String userId) {
        return JWT.create()
                .withSubject(userId)
                .withClaim("user", true)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtAccessExpirationTimeSec * 1000))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    public String generateRefreshToken(String userId) {
        return JWT.create()
                .withSubject(userId)
                .withClaim("user", true)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpirationTimeSec * 1000))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    public Pair<String, String> refreshTokens(String refreshToken) {
        try {
            String userId = getUserIdFromToken(refreshToken);
            if (userDao.tokenMatches(userId, refreshToken)) {
                String token = generateAccessToken(userId);
                String newRefreshToken = generateRefreshToken(userId);
                userDao.updateToken(userId, newRefreshToken);
                return new Pair<>(token, newRefreshToken);
            }
            return null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getUserIdFromToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(token);
        Claim user = jwt.getClaim("user");
        if (user.isNull() || !user.asBoolean()) {
            return null;
        }
        return jwt.getSubject();
    }
}
