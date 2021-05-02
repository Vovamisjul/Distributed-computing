package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.models.DeviceDetails;
import com.vovamisjul.dserver.models.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserDao {

    // language=SQL
    private static final String SELECT_USER = "SELECT * FROM `users` WHERE `username`=?";
    // language=SQL
    private static final String SELECT_TOKEN = "SELECT `refresh_token` FROM `users` WHERE `id`=?";
    // language=SQL
    private static final String UPDATE_TOKEN = "UPDATE `users` SET `refresh_token`=? WHERE `id`=?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserDetails getUserByUsername(String username) {
        return jdbcTemplate.query(SELECT_USER,
                rs -> {
                    if (rs.next()) {
                        return new UserDetails(rs.getInt("id"), username, rs.getString("password"));
                    }
                    return null;
                },
                username);
    }

    @SuppressWarnings("ConstantConditions")
    public boolean tokenMatches(String userId, String token) {
        return jdbcTemplate.query(
                SELECT_TOKEN,
                rs -> {
                    if (rs.next()) {
                        return token.equals(rs.getString("refresh_token"));
                    }
                    return false;
                },
                userId
        );
    }

    public void updateToken(String userId, String token) {
        jdbcTemplate.update(
                UPDATE_TOKEN,
                token,
                userId
        );
    }
}
