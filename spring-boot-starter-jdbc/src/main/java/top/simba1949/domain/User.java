package top.simba1949.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 表名：user
 * 表注释：用户信息表
*/
@Data
@Table(name = "user")
public class User extends BaseDomain implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 业务主键
     */
    @Column(name = "code")
    private String code;

    /**
     * 用户登录名
     */
    @Column(name = "username")
    private String username;

    /**
     * 密码
     */
    @Column(name = "password")
    private String password;

    /**
     * 真实姓名
     */
    @Column(name = "real_name")
    private String realName;

    /**
     * 用户昵称
     */
    @Column(name = "nick_name")
    private String nickName;

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "birthday")
    private Date birthday;

    /**
     * 地址
     */
    @Column(name = "address")
    private String address;

    /**
     * 手机号码
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 邮件
     */
    @Column(name = "email")
    private String email;

    private static final long serialVersionUID = 1L;
}