package top.simba1949.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.simba1949.domain.Permission;
import top.simba1949.domain.Role;
import top.simba1949.domain.User;
import top.simba1949.service.UserService;
import top.simba1949.util.UserUtils;

import java.sql.SQLException;
import java.util.Collections;

/**
 * @author anthony
 * @date 2023/1/6
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("insert")
    public String insert(@RequestBody User user) throws IllegalAccessException {
        user.fillData4Insert(UserUtils.systemUser());

        userService.insert(user);

        return "SUCCESS";
    }

//    @GetMapping("update")
//    public String update(){
//
//    }
//
//    @GetMapping("get")
//    public String get(){
//
//    }
//
//    @GetMapping("list")
//    public void list(){
//
//    }
//
    @GetMapping("batchInsert")
    public String batchInsert() {
        Permission permission = new Permission();
        permission.setCode("PERMISSION-CODE");
        permission.setPermissionName("权限系统名称");
        permission.setPermissionUrl("PERMISSION/URL");
        permission.fillData4Insert(UserUtils.systemUser());

        Role role = new Role();
        role.setCode("ROLE-CODE");
        role.setRoleName("ROLE-NAME");
        role.fillData4Insert(UserUtils.systemUser());

        userService.batchInsert(Collections.singletonList(permission), Collections.singletonList(role));

        return "SUCCESS";
    }
}
