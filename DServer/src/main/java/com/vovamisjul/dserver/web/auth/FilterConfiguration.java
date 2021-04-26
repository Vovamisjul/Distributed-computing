package com.vovamisjul.dserver.web.auth;

import com.vovamisjul.dserver.web.filters.JWTDeviceAuthFilter;
import com.vovamisjul.dserver.web.filters.JWTTaskAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class FilterConfiguration {

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

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JWTTaskAuthFilter> tasksFilter(){
        FilterRegistrationBean<JWTTaskAuthFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(jwtTaskAuthFilter);
        registrationBean.addUrlPatterns("/queue/*");


        return registrationBean;
    }
}
