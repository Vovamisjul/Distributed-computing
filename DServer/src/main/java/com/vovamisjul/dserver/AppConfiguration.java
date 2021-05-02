package com.vovamisjul.dserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.PostConstruct;

@Configuration
public class AppConfiguration {

    private DriverManagerDataSource driverManagerDataSource;

    @Autowired
    private Environment env;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        driverManagerDataSource = new DriverManagerDataSource(
                env.getProperty("db.url"),
                env.getProperty("db.user"),
                env.getProperty("db.password")
        );
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(driverManagerDataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(driverManagerDataSource);
    }

}
