package com.vovamisjul.dserver.web.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.vovamisjul.dserver.dao.DeviceDetailsService;
import com.vovamisjul.dserver.web.auth.JwtProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JWTTaskAuthFilter extends HttpFilter {

    private static Logger LOG = LogManager.getLogger(JWTTaskAuthFilter.class);

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    public void doFilter(HttpServletRequest request,
                         HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        if (token != null) {
            // parse the token.
            try {
                String user = jwtProvider.getLoginFromUserToken(token.replace("Bearer ", ""));
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
