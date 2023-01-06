package top.simba1949.domain;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

/**
 * 表名：role_permission
 * 表注释：角色权限中间表
*/
@Data
@Table(name = "role_permission")
public class RolePermission extends BaseDomain implements Serializable {
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
     * 角色主键
     */
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 权限主键
     */
    @Column(name = "permission_id")
    private Long permissionId;

    private static final long serialVersionUID = 1L;
}