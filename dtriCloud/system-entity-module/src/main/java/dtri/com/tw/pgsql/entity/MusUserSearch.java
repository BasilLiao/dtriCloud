package dtri.com.tw.pgsql.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * @author Allen Chen
 * @description 用戶操作記憶表
 *              用途: 記錄使用者操作習慣
 *              資料來源:使用者操作時同步寫入資料
 */
@Entity
@Data
@Table(name = "mus_user_search")
public class MusUserSearch {
    public MusUserSearch() {
        // 共用型
        this.syscdate = new Date();
        this.syscuser = "system";
        this.sysmdate = new Date();
        this.sysmuser = "system";
        this.sysodate = new Date();
        this.sysouser = "system";

        this.sysheader = false;
        this.sysstatus = 0;
        this.syssort = 0;// 欄位?排序
        this.sysnote = "";
    }

    @Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date syscdate;
    @Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String syscuser;
    @Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date sysmdate;
    @Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String sysmuser;
    @Column(name = "sys_o_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date sysodate;
    @Column(name = "sys_o_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String sysouser;

    @Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
    private Boolean sysheader;
    @Column(name = "sys_status", nullable = false, columnDefinition = "int default 0")
    private Integer sysstatus;
    @Column(name = "sys_sort", nullable = false, columnDefinition = "int default 0")
    private Integer syssort;
    @Column(name = "sys_note", nullable = false, columnDefinition = "text default ''")
    private String sysnote;

    // 主鍵：mus_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mus_id", nullable = false)
    private Long musid;

    // 使用者 ID：mus_u_id
    @Column(name = "mus_u_id", nullable = false)
    private Long musuid;

    // 使用者查詢條件 (JSON 字串)：mus_search
    @Column(name = "mus_search", nullable = false, columnDefinition = "TEXT")
    private String mussearch;
}
