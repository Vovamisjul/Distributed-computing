package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.models.DeviceDetails;
import com.vovamisjul.dserver.models.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDao {

    // language=SQL
    private static String SELECT_USER = "SELECT * FROM `users` WHERE `username`=?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserDetails getUserByUsername(String username) {
        return jdbcTemplate.query(SELECT_USER,
                rs -> {
                    if (rs.next()) {
                        UserDetails user = new UserDetails(rs.getInt("id"), username, rs.getString("password"));
                        return user;
                    }
                    return null;
                },
                username);
    }
}
