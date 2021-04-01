package com.vovamisjul.dserver.web.auth;

import com.auth0.jwt.JWT;
import com.vovamisjul.dserver.dao.DeviceController;
import com.vovamisjul.dserver.dao.DeviceDao;
import com.vovamisjul.dserver.dao.UserDao;
import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.models.DeviceDetails;
import com.vovamisjul.dserver.models.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.UUID;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.lang.Long.parseLong;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class AuthController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public DeviceDao deviceDao;

    @Autowired
    public UserDao userDao;

    @Autowired
    private JwtProvider jwtProvider;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<Response> register(@Valid @RequestBody Registration registration) {
        String id = UUID.randomUUID().toString();
        deviceDao.addNewDevice(id, bCryptPasswordEncoder.encode(registration.password));
        return new ResponseEntity<>(new Response(id, jwtProvider.generateToken(id)), CREATED);
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
            return new ResponseEntity<>(new Response(authentication.id, jwtProvider.generateToken(authentication.id)), CREATED);
        }
        return new ResponseEntity<>(UNAUTHORIZED);
    }

    @Validated
    private static class Authentication {
        @NotNull
        public String id;
        @NotNull
        public String password;
    }

    @RequestMapping(value = "/userlogin", method = RequestMethod.POST)
    public ResponseEntity<Response> loginUser(@Valid @RequestBody UserAuthentication authentication) {
        UserDetails user = userDao.getUserByUsername(authentication.username);
        if (user != null && bCryptPasswordEncoder.matches(authentication.password, user.getPassword())) {
            return new ResponseEntity<>(new Response(Integer.toString(user.getId()), jwtProvider.generateUserToken(authentication.username)), CREATED);
        }
        return new ResponseEntity<>(UNAUTHORIZED);
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
        public String token;

        public Response(String id, String token) {
            this.id = id;
            this.token = token;
        }
    }
}
