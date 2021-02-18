package com.vovamisjul.dserver.web.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.vovamisjul.dserver.dao.DeviceDetailsService;
import com.vovamisjul.dserver.tcp.ConnectionHandler;
import com.vovamisjul.dserver.web.auth.JwtProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JWTAuthorizationFilter extends HttpFilter {

    private static Logger LOG = LogManager.getLogger(JWTAuthorizationFilter.class);
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private DeviceDetailsService deviceDetailsService;

    @Override
    public void doFilter(HttpServletRequest request,
                         HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        if (token != null) {
            // parse the token.
            try {
                String user = jwtProvider.getLoginFromToken(token.replace("Bearer ", ""));
                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                }
            } catch (JWTVerificationException e) {
                return null;
            }
            return null;
        }
        return null;
    }
}