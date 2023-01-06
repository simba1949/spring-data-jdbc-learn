package top.simba1949.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import top.simba1949.domain.Permission;
import top.simba1949.domain.Role;
import top.simba1949.domain.User;
import top.simba1949.util.SqlUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author anthony
 * @date 2023/1/6
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(User user) throws IllegalAccessException {
        String insertSql = SqlUtils.getInsertSql(User.class);
        Object[] insertVal = SqlUtils.getInsertVal(user);

        jdbcTemplate.batchUpdate(insertSql, Collections.singletonList(insertVal));
    }

    public void batchInsert(List<Permission> permissions, List<Role> roles) {
        String insertSql4Permission = SqlUtils.getInsertSql(Permission.class);
        String insertSql4Role = SqlUtils.getInsertSql(Role.class);

        List<Object[]> permissionVals = permissions.stream()
                .map(SqlUtils::getInsertVal)
                .collect(Collectors.toList());

        List<Object[]> roleVals = roles.stream()
                .map(SqlUtils::getInsertVal)
                .collect(Collectors.toList());

        Connection connection = null;
        try {
            DataSource dataSource = jdbcTemplate.getDataSource();
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            jdbcTemplate.batchUpdate(insertSql4Permission, permissionVals);
            jdbcTemplate.batchUpdate(insertSql4Role, roleVals);

            connection.commit();
            connection.setAutoCommit(true);
        }catch (Exception e){

        }finally {
            if (null != connection){
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.info("关闭连接失败{}", e.getMessage(), e);
                }
            }
        }


    }
}
