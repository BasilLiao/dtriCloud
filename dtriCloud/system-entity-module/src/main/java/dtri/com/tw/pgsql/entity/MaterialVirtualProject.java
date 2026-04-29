package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * @author Allen Chen
 * @description 設置-產品系列主建 (Material Virtual Project)
 *              用途：物控人員自訂「虛擬專案」，可指定複數 90BOM / 機型，
 *              展開所有物料後分類為主物料/副物料，並模擬齊套率與可生產套數。
 *
 *              ---業務欄位---
 *              mvp_id : 主KEY (PK)
 *              mvp_name : 系列名稱 (使用者自訂)
 *              mvp_boms : 指定的 90BOM 清單 (JSON陣列)
 *              mvp_main_materials : 主物料清單 (JSON: 料號/數量/製程別)
 *              mvp_sub_materials : 副物料清單 (JSON: 料號/數量/分類)
 *              mvp_use_replacement : 是否套用替代物料表
 *
 *              [已移除] mvp_exclude / mvp_include / mvp_sim_orders / mvp_sim_time
 *              原因：「排除條件」與「模擬設定」皆已改為前端運算後直接儲存在
 *              mvp_main_materials / mvp_sub_materials 各物料的 JSON 屬性內，
 *              毋需在資料表額外保存這些中間規則欄位。
 */
@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "material_virtual_project")
public class MaterialVirtualProject {

    public MaterialVirtualProject() {
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
    @Column(name = "mvp_id", nullable = false)
    private Long mvpid;

    // [系列名稱] 使用者自定義的專案名稱
    @Column(name = "mvp_name", nullable = false, columnDefinition = "varchar(150) default ''")
    private String mvpname;

    // [90BOM 清單] 指定的複數 90BOM / 機型代號 (JSON陣列)
    @Column(name = "mvp_boms", nullable = false, columnDefinition = "TEXT")
    private String mvpboms;

    // [排除條件/設定] 包含層級、排除首碼、倉庫設定 (JSON 物件)
    @Column(name = "mvp_exclude", nullable = false, columnDefinition = "TEXT")
    private String mvpexclude;

    // [包含料號] 指定要加入的料號 (JSON 陣列)
    @Column(name = "mvp_include", nullable = false, columnDefinition = "TEXT")
    private String mvpinclude;

    // [主物料清單] 每個BOM都有的共通必備料 (JSON)
    @Column(name = "mvp_main_materials", nullable = false, columnDefinition = "TEXT")
    private String mvpmainmaterials;

    // [副物料清單] 部分BOM才有的選配料 (JSON)
    @Column(name = "mvp_sub_materials", nullable = false, columnDefinition = "TEXT")
    private String mvpsubmaterials;

    // [是否套用替代物料] 計算齊套時是否自動套用 MaterialReplacement 規則
    @Column(name = "mvp_use_replacement", nullable = false, columnDefinition = "boolean default false")
    private Boolean mvpusereplacement;

    // [模擬單據設定] 紀錄來源單據規則 (JSON 物件)
    @Column(name = "mvp_sim_orders", nullable = false, columnDefinition = "TEXT")
    private String mvpsimorders;

    // [模擬時間參數] 紀錄虛擬數量與完工日 (JSON 物件)
    @Column(name = "mvp_sim_time", nullable = false, columnDefinition = "TEXT")
    private String mvpsimtime;
}
