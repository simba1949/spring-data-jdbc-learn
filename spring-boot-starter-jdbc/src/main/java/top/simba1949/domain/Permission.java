package top.simba1949.domain;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

/**
 * 表名：permission
 * 表注释：角色表
*/
@Data
@Table(name = "permission")
public class Permission extends BaseDomain implements Serializable {
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
     * 权限描述名称
     */
    @Column(name = "permission_name")
    private String permissionName;

    /**
     * 权限url
     */
    @Column(name = "permission_url")
    private String permissionUrl;

    private static final long serialVersionUID = 1L;
}