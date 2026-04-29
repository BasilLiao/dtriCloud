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
 * @description 設置-製程對照表 (Material Process)
 *              用途：針對 90BOM 內務料號與製程、區塊特性的對應規則設定
 */
@Entity
@Data
@Table(name = "material_process")
public class MaterialProcess {

    public MaterialProcess() {
        // ================= 1. 系統共用欄位初始化 =================
        this.syscdate = new Date();
        this.syscuser = "system";
        this.sysmdate = new Date();
        this.sysmuser = "system";
        this.sysodate = new Date();
        this.sysouser = "system";

        this.sysheader = false; // 預設 false: 一般模式
        this.sysstatus = 0; // 0:正常
        this.syssort = 0;
        this.sysnote = "";
    }

    // ======================== 2. 系統共用欄位 (System Fields) ========================
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

    // ======================== 3. 業務功能欄位 (Business Fields) ========================

    // [PK] 主KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mp_id", nullable = false)
    private Long mpid;

    // [製程代號] Ex: ASM/MSR...
    @Column(name = "mp_name", nullable = false, length = 50)
    private String mpname;

    // [製程對照名稱/區塊類型] Ex: 包裝/加工/組裝 (對應功能說明的"區塊類型")
    // 定義為 TEXT 類型
    @Column(name = "mp_group", nullable = false, columnDefinition = "TEXT")
    private String mpgroup;

    // [其他條件/特殊條件] JSON格式 (對應功能說明的"特殊條件")
    // 定義為 TEXT 類型
    @Column(name = "mp_other", nullable = false, columnDefinition = "TEXT")
    private String mpother;
}