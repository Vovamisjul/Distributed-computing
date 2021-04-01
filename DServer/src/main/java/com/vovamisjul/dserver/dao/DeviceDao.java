package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.models.DeviceDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DeviceDao {

    // language=SQL
    private static String INSERT_DEVISE = "INSERT FROM `devices` (`id`, `password`) VALUE (?,?)";
    // language=SQL
    private static String SELECT_DEVISE = "SELECT `devices`.`id`, `performance_rate`, GROUP_CONCAT(DISTINCT `device_tasks`.`task_id` SEPARATOR ', ') AS `tasks` FROM `devices` LEFT JOIN `device_tasks` ON `devices`.`id` = `device_tasks`.`device_id` WHERE `devices`.`id`=?";
    // language=SQL
    private static String SELECT_DEVISES_DETAILS = "SELECT `id`, `password` FROM `devices` WHERE `id`=?";
    // language=SQL
    private static String CLEAR_DEVICE_TASKS = "DELETE FROM `device_tasks` WHERE `device_id`=?";
    // language=SQL
    private static String ADD_DEVISE_TASKS = "INSERT INTO `device_tasks` (`device_id`, `task_id`) values (?,?)";
    // language=SQL
    private static String UPDATE_PERFORMANCE_RATE = "UPDATE `devices` SET `performance_rate`=? WHERE `id`=?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void updatePerformance(String id, float power) {
        jdbcTemplate.update(UPDATE_PERFORMANCE_RATE, power, id);
    }

    public void addNewDevice(String id, String password) {
        jdbcTemplate.update(INSERT_DEVISE, id, password);
    }

    @Nullable
    public Device getDevice(String id) {
        return jdbcTemplate.query(SELECT_DEVISE,
                rs -> {
                    if (rs.next()) {
                        Device device = new Device(id);
                        String rate = rs.getString("performance_rate");
                        if (rate != null) {
                            device.setPerformanceRate(Float.valueOf(rate));
                        }
                        String tasks = rs.getString("tasks");
                        if (tasks != null) {
                            device.setAvaliableTasks(new HashSet<>(Arrays.asList(tasks.split(", "))));
                        }
                        return device;
                    }
                    return null;
                },
                id);
    }

    public DeviceDetails getDeviceDetails(String id) {
        return jdbcTemplate.query(SELECT_DEVISES_DETAILS,
                rs -> {
                    if (rs.next()) {
                        return new DeviceDetails(id, rs.getString("password"));
                    }
                    return null;
                },
                id);
    }

    public void updatePossibleTasks(String deviceId, List<String> taskIds) {
        jdbcTemplate.update(CLEAR_DEVICE_TASKS, deviceId);
        List<Object[]> values = new ArrayList<>();
        for (String task: taskIds) {
            values.add(new Object[]{deviceId, task});
        }
        jdbcTemplate.batchUpdate(ADD_DEVISE_TASKS, values);
    }
}
