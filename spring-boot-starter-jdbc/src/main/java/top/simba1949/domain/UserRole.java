package top.simba1949.domain;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

/**
 * 表名：user_role
 * 表注释：用户-角色关系中间表
*/
@Data
@Table(name = "user_role")
public class UserRole extends BaseDomain implements Serializable {
    /**
     * 主键
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Long id;

    /**
     * 用户主键
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 角色主键
     */
    @Column(name = "role_id")
    private Long roleId;

    private static final long serialVersionUID = 1L;
}