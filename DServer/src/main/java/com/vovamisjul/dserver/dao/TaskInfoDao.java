package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.objects.CreatedTaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskInfoDao {

    @Autowired
    private TaskControllerRepository taskControllerRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    // language=SQL
    private static final String ADD_INFO = "INSERT INTO `task_info` (copy_id, task_id, user_id, params, comment, created) VALUE (?,?,?,?,?,NOW())";
    // language=SQL
    private static final String GET_INFOS_IN = "SELECT `task_info`.*, username FROM `task_info` JOIN `users` on `users`.`id` = `task_info`.`user_id` WHERE copy_id IN (:ids)";

    public void addTaskInfo(String copyId, String taskId, String userId, String params, String comment) {
        jdbcTemplate.update(ADD_INFO, copyId, taskId, userId, params, comment);
    }

    public List<CreatedTaskInfo> getTaskInfos(List<String> copyIds) {
        SqlParameterSource parameters = new MapSqlParameterSource("ids", copyIds);

        return namedParameterJdbcTemplate.query(
                GET_INFOS_IN,
                parameters,
                (rs, rowNum) -> new CreatedTaskInfo(
                        taskControllerRepository.getTaskInfo(rs.getString("task_id")),
                        rs.getString("username"),
                        rs.getString("params"),
                        rs.getDate("created"),
                        rs.getString("comment")
                )
        );
    }
}
