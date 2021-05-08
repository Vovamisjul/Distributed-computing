package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import com.vovamisjul.dserver.tasks.objects.FinishedTaskInfo;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class TaskResultsDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TaskControllerRepository taskControllerRepository;
    // language=SQL
    private static final String INSERT_RESULT = "INSERT INTO `task_results` (task_copy_id, result, finished) VALUE (?,?,NOW())";
    // language=SQL
    private static final String SELECT_RESULT_BY_PARAMS = "SELECT `copy_id`, `result` " +
            "FROM `task_results` " +
            "         JOIN `task_info` ON `task_info`.`copy_id` = `task_results`.`task_copy_id` " +
            "WHERE `task_id` = ? " +
            "  AND `params` = ?";
    // language=SQL
    private static final String SELECT_RESULT_BY_COPY_ID = "SELECT `result` FROM `task_results` WHERE `task_copy_id`=?";
    // language=SQL
    private static final String SELECT_RESULT_BY_AUTHOR = "SELECT `task_info`.*, `task_results`.*, username " +
            "FROM `task_results` " +
            "         JOIN `task_info` ON `task_info`.`copy_id` = `task_results`.`task_copy_id` " +
            "         JOIN `users` ON `users`.`id` = `task_info`.`user_id` " +
            "WHERE `user_id` = ?";

    /**
     * @return Pair: value0 - copy_id, value1 - result
     */
    public Pair<String, String> getResultByParams(String taskId, String params) {
        return jdbcTemplate.query(SELECT_RESULT_BY_PARAMS,
                rs -> {
                    if (rs.next()) {
                        return new Pair<>(rs.getString("copy_id"), rs.getString("result"));
                    }
                    return null;
                },
                taskId,
                params);
    }

    public String getResultByCopyId(String copyId) {
        return jdbcTemplate.query(SELECT_RESULT_BY_COPY_ID,
                rs -> {
                    if (rs.next()) {
                        return rs.getString("result");
                    }
                    return null;
                },
                copyId);
    }

    public void saveResult(String copyId, String result) {
        jdbcTemplate.update(INSERT_RESULT, copyId, result);
    }

    public List<FinishedTaskInfo> getUserResults(String userId) {
        return jdbcTemplate.query(
                SELECT_RESULT_BY_AUTHOR,
                rs -> {
                    List<FinishedTaskInfo> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(new FinishedTaskInfo(
                                taskControllerRepository.getTaskInfo(rs.getString("task_id")),
                                rs.getString("copy_id"),
                                rs.getString("username"),
                                rs.getString("params"),
                                rs.getTimestamp("created", Calendar.getInstance()),
                                rs.getString("comment"),
                                rs.getString("result"),
                                rs.getTimestamp("finished", Calendar.getInstance()))
                        );
                    }
                    return result;
                },
                userId);
    }
}
