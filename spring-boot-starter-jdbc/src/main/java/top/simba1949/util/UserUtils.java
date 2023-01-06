package top.simba1949.util;

import top.simba1949.domain.User;

/**
 * @author anthony
 * @date 2023/1/6
 */
public class UserUtils {

    public static User systemUser(){
        User user = new User();
        user.setId(0L);
        user.setCode("SYSTEM");
        user.setUsername("SYSTEM");
        user.setPassword("SYSTEM");
        user.setRealName("SYSTEM");
        user.setNickName("SYSTEM");
        user.setAddress("SYSTEM");
        user.setPhone("SYSTEM");
        user.setEmail("SYSTEM");
        return user;
    }
}
