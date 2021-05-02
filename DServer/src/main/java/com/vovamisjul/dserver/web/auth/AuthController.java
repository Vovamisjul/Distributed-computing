package com.vovamisjul.dserver.web.auth;

import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.dao.UserDao;
import com.vovamisjul.dserver.models.DeviceDetails;
import com.vovamisjul.dserver.models.UserDetails;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
public class AuthController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public DeviceDao deviceDao;

    @Autowired
    public UserDao userDao;

    @Autowired
    private JwtUserProvider jwtUserProvider;

    @Autowired
    private JwtDeviceProvider jwtDeviceProvider;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<Response> register(@Valid @RequestBody Registration registration) {
        String id = UUID.randomUUID().toString();
        String refreshToken = jwtDeviceProvider.generateRefreshToken(id);
        deviceDao.addNewDevice(id, bCryptPasswordEncoder.encode(registration.password), refreshToken);
        return new ResponseEntity<>(
                new Response(
                        id,
                        jwtDeviceProvider.generateAccessToken(id),
                        refreshToken
                ),
                CREATED);
    }

    @Validated
    private static class Registration {
        @NotNull
        public String password;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<Response> login(@Valid @RequestBody Authentication authentication) {
        DeviceDetails device = deviceDao.getDeviceDetails(authentication.id);
        if (device != null && bCryptPasswordEncoder.matches(authentication.password, device.getPassword())) {
            return new ResponseEntity<>(
                    new Response(
                            jwtDeviceProvider.generateAccessToken(authentication.id),
                            jwtDeviceProvider.generateAndUpdateRefreshToken(authentication.id)
                    ), CREATED);
        }
        return new ResponseEntity<>(UNAUTHORIZED);
    }

    @RequestMapping(value = "/refreshtoken", method = RequestMethod.POST)
    public ResponseEntity<Response> refreshToken(@Valid @RequestBody Refresh refresh) {
        Pair<String, String> tokens = jwtDeviceProvider.refreshTokens(refresh.refreshToken);
        if (tokens == null) {
            return new ResponseEntity<>(UNAUTHORIZED);
        }
        return new ResponseEntity<>(
                new Response(
                        tokens.getValue0(),
                        tokens.getValue1()
                ), CREATED);
    }

    @Validated
    private static class Authentication {
        @NotNull
        public String id;
        @NotNull
        public String password;
    }

    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public ResponseEntity<Response> loginUser(@Valid @RequestBody UserAuthentication authentication) {
        UserDetails user = userDao.getUserByUsername(authentication.username);
        if (user != null && bCryptPasswordEncoder.matches(authentication.password, user.getPassword())) {
            String userId = String.valueOf(user.getId());
            String refresh = jwtUserProvider.generateRefreshToken(userId);
            userDao.updateToken(userId, refresh);
            return new ResponseEntity<>(
                    new Response(
                            userId,
                            jwtUserProvider.generateAccessToken(userId),
                            refresh
                    ), CREATED);
        }
        return new ResponseEntity<>(UNAUTHORIZED);
    }

    @RequestMapping(value = "/user/refreshtoken", method = RequestMethod.POST)
    public ResponseEntity<Response> refreshUserToken(@Valid @RequestBody Refresh refresh) {
        Pair<String, String> tokens = jwtUserProvider.refreshTokens(refresh.refreshToken);
        if (tokens == null) {
            return new ResponseEntity<>(UNAUTHORIZED);
        }
        return new ResponseEntity<>(
                new Response(
                        tokens.getValue0(),
                        tokens.getValue1()
                ), CREATED);
    }

    @Validated
    private static class UserAuthentication {
        @NotNull
        public String username;
        @NotNull
        public String password;
    }


    private static class Response {
        public String id;
        public String accessToken;
        public String refreshToken;

        public Response(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public Response(String id, String accessToken, String refreshToken) {
            this.id = id;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Validated
    private static class Refresh {
        @NotNull
        public String refreshToken;
    }
}
