package top.simba1949.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * @author anthony
 * @date 2023/1/6
 */
@Data
public class BaseDomain implements Serializable {
    private static final long serialVersionUID = 5274028003313313083L;

    @Column(name = "bl_enable")
    private Byte blEnable;
    @Column(name = "bl_delete")
    private Byte blDelete;
    @Column(name = "version")
    private Long version;
    @Column(name = "gmt_create")
    private Date gmtCreate;
    @Column(name = "creator")
    private String creator;
    @Column(name = "creator_id")
    private Long creatorId;
    @Column(name = "gmt_modified")
    private Date gmtModified;
    @Column(name = "modifier")
    private String modifier;
    @Column(name = "modifier_id")
    private Long modifierId;

    /**
     * 补填数据-新增
     * @param user
     */
    public void fillData4Insert(User user){
        this.blEnable = (byte)1;
        this.blDelete = (byte)0;
        this.version = 0L;

        this.gmtCreate = new Date();
        this.creator = user.getRealName();
        this.creatorId = user.getId();

        fillData4Update(user);
    }

    /**
     * 补填数据-更新
     * @param user
     */
    public void fillData4Update(User user){
        this.gmtModified = new Date();
        this.modifier = user.getRealName();
        this.modifierId = user.getId();
    }
}
