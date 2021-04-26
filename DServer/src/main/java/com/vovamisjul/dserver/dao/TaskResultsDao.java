package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.models.Device;
import com.vovamisjul.dserver.tasks.RunningTaskInfo;
import com.vovamisjul.dserver.tasks.TaskControllerRepository;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TaskResultsDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TaskControllerRepository taskControllerRepository;
    // language=SQL
    private static final String INSERT_RESULT = "INSERT INTO `task_results` (copy_id, result, task_id, params, user_id, finished) VALUE (?,?,?,?,?,NOW())";
    // language=SQL
    private static final String SELECT_RESULT_BY_PARAMS = "SELECT `copy_id`, `result` FROM `task_results` WHERE `task_id`=? AND `params`=?";
    // language=SQL
    private static final String SELECT_RESULT_BY_COPY_ID = "SELECT `result` FROM `task_results` WHERE `copy_id`=?";
    // language=SQL
    private static final String SELECT_RESULT_BY_AUTHOR = "SELECT * FROM `task_results` WHERE `user_id`=?";

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

    public void saveResult(String copyId, String params, String taskId, String result, String userId) {
        jdbcTemplate.update(INSERT_RESULT, copyId, result, taskId, params, userId);
    }

    public List<Triplet<RunningTaskInfo, String, Date>> getUserResults(String userId) {
        return jdbcTemplate.query(
                SELECT_RESULT_BY_AUTHOR,
                rs -> {
                    List<Triplet<RunningTaskInfo, String, Date>> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(new Triplet<>(
                                new RunningTaskInfo(
                                        taskControllerRepository.getTaskInfo(rs.getString("task_id")),
                                        rs.getString("copy_id"),
                                        userId),
                                rs.getString("result"),
                                rs.getDate("finished")
                                ));
                    }
                    return result;
                },
                userId);
    }
}
