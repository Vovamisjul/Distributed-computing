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
import static java.lang.Long.parseLong;

@Component
public class JwtDeviceProvider {

    @Autowired
    public DeviceDao deviceDao;

    @Value("${jwt.password}")
    private String jwtSecret;
    @Value("${jwt.accessExpirationTimeSec}")
    private long jwtAccessExpirationTimeSec;
    @Value("${jwt.refreshExpirationTimeSec}")
    private long jwtRefreshExpirationTimeSec;

    public String generateAccessToken(String deviceId) {
        return JWT.create()
                .withSubject(deviceId)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtAccessExpirationTimeSec * 1000))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    public String generateRefreshToken(String subject) {
        return JWT.create()
                .withSubject(subject)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpirationTimeSec * 1000))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    public String generateAndUpdateRefreshToken(String deviceId) {
        String token = JWT.create()
                .withSubject(deviceId)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtRefreshExpirationTimeSec * 1000))
                .sign(HMAC512(jwtSecret.getBytes()));
        deviceDao.updateToken(deviceId, token);
        return token;
    }

    public Pair<String, String> refreshTokens(String refreshToken) {
        try {
            String deviceId = getDeviceIdFromToken(refreshToken);
            if (deviceDao.tokenMatches(deviceId, refreshToken)) {
                String token = generateAccessToken(deviceId);
                String newRefreshToken = generateRefreshToken(deviceId);
                deviceDao.updateToken(deviceId, newRefreshToken);
                return new Pair<>(token, refreshToken);
            }
            return null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String getDeviceIdFromToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(token);
        Claim user = jwt.getClaim("user");
        if (!user.isNull()) {
            return null;
        }
        return jwt.getSubject();
    }
}
