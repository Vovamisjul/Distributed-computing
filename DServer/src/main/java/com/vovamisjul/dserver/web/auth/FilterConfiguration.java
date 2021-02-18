package com.vovamisjul.dserver.web.auth;

import com.vovamisjul.dserver.web.filters.JWTDeviceAuthFilter;
import com.vovamisjul.dserver.web.filters.JWTTaskAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class FilterConfiguration {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JWTDeviceAuthFilter jwtDeviceAuthFilter;
    @Autowired
    private JWTTaskAuthFilter jwtTaskAuthFilter;

    @Bean
    public FilterRegistrationBean<JWTDeviceAuthFilter> devicesFilter(){
        FilterRegistrationBean<JWTDeviceAuthFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(jwtDeviceAuthFilter);
        registrationBean.addUrlPatterns("/settings/*", "/messages");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JWTTaskAuthFilter> tasksFilter(){
        FilterRegistrationBean<JWTTaskAuthFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(jwtTaskAuthFilter);
        registrationBean.addUrlPatterns("/tasks/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);


        return registrationBean;
    }
}
