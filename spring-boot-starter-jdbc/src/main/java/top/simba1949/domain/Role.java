package top.simba1949.domain;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

/**
 * 表名：role
 * 表注释：角色表
*/
@Data
@Table(name = "role")
public class Role extends BaseDomain implements Serializable {
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
     * 角色名称
     */
    @Column(name = "role_name")
    private String roleName;

    private static final long serialVersionUID = 1L;
}