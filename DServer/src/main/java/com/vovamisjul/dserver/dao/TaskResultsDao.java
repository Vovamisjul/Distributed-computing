package com.vovamisjul.dserver.dao;

import com.vovamisjul.dserver.models.Device;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TaskResultsDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    // language=SQL
    private static String INSERT_RESULT = "INSERT INTO `task_results` (copy_id, result, task_id, params) VALUE (?,?,?,?)";
    // language=SQL
    private static String SELECT_RESULT_BY_PARAMS = "SELECT `copy_id`, `result` FROM `task_results` WHERE `task_id`=? AND `params`=?";
    // language=SQL
    private static String SELECT_RESULT_BY_COPY_ID = "SELECT `result` FROM `task_results` WHERE `copy_id`=?";

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

    public void saveResult(String copyId, String params, String taskId, String result) {
        jdbcTemplate.update(INSERT_RESULT, copyId, result, taskId, params);
    }
}
