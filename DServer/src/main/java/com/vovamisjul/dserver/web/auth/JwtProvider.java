package com.vovamisjul.dserver.web.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.vovamisjul.dserver.models.DeviceDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.lang.Long.parseLong;

@Component
public class JwtProvider {

    @Value("${jwt.password}")
    private String jwtSecret;
    @Value("${jwt.expirationTime}")
    private long jwtExpirationTime;

    public String generateToken(String deviceId) {
        return JWT.create()
                .withSubject(deviceId)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    public String generateUserToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withClaim("user", true)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .sign(HMAC512(jwtSecret.getBytes()));
    }

    public String getDeviceIdFromToken(String token) {
        return JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(token)
                .getSubject();
    }

    public String getLoginFromUserToken(String token) {
        DecodedJWT jwt = JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(token);
        Claim user = jwt.getClaim("user");
        if (user.isNull() || user.asBoolean() == false) {
            return null;
        }
        return jwt.getSubject();
    }

    public void verifyToken(String token) {
        JWT.require(Algorithm.HMAC512(jwtSecret.getBytes()))
                .build()
                .verify(token);
    }
}
