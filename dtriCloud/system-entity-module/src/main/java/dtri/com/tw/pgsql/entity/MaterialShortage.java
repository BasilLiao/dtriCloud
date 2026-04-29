package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Allen Chen
 * @description 料件缺料進貨預計表主要資料
 *              用途: 反正料件未來缺料狀況
 *              資料來源:原ERP系統資料計算後倒入
 */
@Entity
@Table(name = "material_shortage")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class MaterialShortage {

    // ---此建構子進行初始化
    public MaterialShortage() {
        // --- 1. 系統欄位初始化 (避免 Null) ---
        this.syscdate = new Date();
        this.sysmdate = new Date();
        this.sysodate = new Date();
        this.syscuser = "system";
        this.sysmuser = "system";
        this.sysouser = "system";
        this.sysheader = false;
        this.sysstatus = 0;
        this.syssort = 0;
        this.sysnote = "";

        // --- 2. 運算欄位初始化 (避免計算時 NPE) ---
        this.invmbmc007 = 0;
        this.syssy003 = 0;
        this.syssy004 = 0;
        this.syssy005 = 0;
        this.syssy001 = 0;
        this.syssy002 = 0;
        this.syssy006 = 0;
        this.syssy007 = 0;
        this.mc004 = 0;

        // 補充缺失欄位初始化
        this.mb036 = 0;
        this.mb039 = 0;
        this.mb040 = 0;
        this.hasreplacement = false;
        this.committedqty = 0;

        // --- 3. 字串欄位初始化 ---
        this.mb001 = "";
        this.mb002 = "";
        this.mb003 = "";
        this.tk000 = "";
        this.tk001 = "";
        this.tk002 = "";
        this.tk003 = "";
        this.syssy008 = "";
        this.syssy009 = "";
        this.syssy011 = "";
        this.ma002 = "";
        this.mb017 = "";
        this.mc002 = "";
        this.mb032 = "";
        this.ta032 = "";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ms_seq_gen")
    @SequenceGenerator(name = "ms_seq_gen", sequenceName = "material_shortage_seq", allocationSize = 1000)
    @Column(name = "msl_id", nullable = false)
    private Long mslid;

    // 料號
    @Column(name = "MB001", nullable = false, length = 255)
    private String mb001;

    // 品名
    @Column(name = "MB002", nullable = false, length = 250)
    private String mb002;

    // 規格
    @Column(name = "MB003", nullable = false, length = 450)
    private String mb003;

    // 預交日
    @Column(name = "TK002", nullable = false, length = 255)
    private String tk002;

    // 庫存數(目前)
    @Column(name = "INVMB_MC007", nullable = false, columnDefinition = "int default 0")
    private Integer invmbmc007;

    // 未領量(當日)
    @Column(name = "SYS_SY003", nullable = false, columnDefinition = "int default 0")
    private Integer syssy003;

    // 未交量(當日)
    @Column(name = "SYS_SY004", nullable = false, columnDefinition = "int default 0")
    private Integer syssy004;

    // 待驗量(目前)
    @Column(name = "SYS_SY005", nullable = false, columnDefinition = "int default 0")
    private Integer syssy005;

    // 未領量(累計)
    @Column(name = "SYS_SY001", nullable = false, columnDefinition = "int default 0")
    private Integer syssy001;

    // 未交量(累計)
    @Column(name = "SYS_SY002", nullable = false, columnDefinition = "int default 0")
    private Integer syssy002;

    // 庫存餘量(當日) [公式:庫存數-未領量]
    @Column(name = "SYS_SY006", nullable = false, columnDefinition = "int default 0")
    private Integer syssy006;

    // 可供餘量(當日) [公式:庫存數+未交量+待驗量-未領量]
    @Column(name = "SYS_SY007", nullable = false, columnDefinition = "int default 0")
    private Integer syssy007;

    // 單別
    @Column(name = "TK000", nullable = false, length = 255)
    private String tk000;

    // 單號
    @Column(name = "TK001", nullable = false, length = 255)
    private String tk001;

    // 產品(品號)
    @Column(name = "TK003", nullable = false, length = 255)
    private String tk003;

    // 配給 (最後 預交日)
    @Column(name = "SYS_SY008", nullable = false, length = 255)
    private String syssy008;

    // 配給 (預交日*未交量)[新單]:單號
    @Column(name = "SYS_SY009", nullable = false, length = 500) // 考量多筆明細，長度調大至 500
    private String syssy009;

    // 平均(6) (六個月均用量)
    @Column(name = "MC004", nullable = false, columnDefinition = "int default 0")
    private Integer mc004;

    // (推薦)請購日*數量
    @Column(name = "SYS_SY011", nullable = false, length = 250)
    private String syssy011;

    // --- 補充缺失之業務欄位 ---

    // L/T (固定供貨天數)
    @Column(name = "MB036", nullable = false, columnDefinition = "int default 0")
    private Integer mb036;

    // MOQ (最低訂購量)
    @Column(name = "MB039", nullable = false, columnDefinition = "int default 0")
    private Integer mb039;

    // MPQ (最小包裝量)
    @Column(name = "MB040", nullable = false, columnDefinition = "int default 0")
    private Integer mb040;

    // 供應商(代號)
    @Column(name = "MB032", nullable = false, length = 255)
    private String mb032; // 調整為Strign 有先資料代號並不是純數字

    // 供應商(名稱)
    @Column(name = "MA002", nullable = false, length = 100)
    private String ma002;

    // 倉別(代號)
    @Column(name = "MB017", nullable = false, length = 50)
    private String mb017;

    // 倉別(名稱)
    @Column(name = "MC002", nullable = false, length = 100)
    private String mc002;

    // 加工廠
    @Column(name = "TA032", nullable = false, length = 50)
    private String ta032;

    // 客戶代號
    @Column(name = "TC004", nullable = false, length = 50)
    private String tc004;

    // 客戶名稱
    @Column(name = "COPMA002", nullable = false, length = 50)
    private String copma002;

    // --- 系統欄位 ---
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date syscdate;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String syscuser;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_m_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date sysmdate;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_m_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String sysmuser;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_o_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date sysodate;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_o_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String sysouser;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_header", nullable = false, columnDefinition = "boolean default false")
    private Boolean sysheader;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_status", nullable = false, columnDefinition = "int default 0")
    private Integer sysstatus;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_sort", nullable = false, columnDefinition = "int default 0")
    private Integer syssort;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "sys_note", nullable = false, columnDefinition = "text default ''")
    private String sysnote;

    // --- 在記憶體中存在，但不寫入資料庫的欄位 ---
    @Transient
    private Boolean hasreplacement; // 標示前端是否有替代料

    @Transient
    private Integer committedqty; // 標示前端試算的佔用數量
}