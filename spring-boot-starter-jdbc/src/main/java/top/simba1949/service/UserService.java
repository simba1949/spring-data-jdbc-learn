package top.simba1949.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.simba1949.domain.Permission;
import top.simba1949.domain.Role;
import top.simba1949.domain.User;
import top.simba1949.util.SqlUtils;

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

    /**
     * 将事务交给 spring 管理
     * @param permissions
     * @param roles
     */
    @Transactional(rollbackFor = Throwable.class)
    public void batchInsert(List<Permission> permissions, List<Role> roles) {
        String insertSql4Permission = SqlUtils.getInsertSql(Permission.class);
        String insertSql4Role = SqlUtils.getInsertSql(Role.class);

        List<Object[]> permissionVals = permissions.stream()
                .map(SqlUtils::getInsertVal)
                .collect(Collectors.toList());

        List<Object[]> roleVals = roles.stream()
                .map(SqlUtils::getInsertVal)
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(insertSql4Permission, permissionVals);
        int i = 1/0;
        jdbcTemplate.batchUpdate(insertSql4Role, roleVals);
    }
}
