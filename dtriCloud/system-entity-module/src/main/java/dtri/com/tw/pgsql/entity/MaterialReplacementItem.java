package dtri.com.tw.pgsql.entity;

import java.util.Date;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 替代規則明細
 * 1A + 2B (SOURCES) = 4C + 5D (TARGETS)
 */
@Entity
@Table(name = "material_replacement_item", indexes = {
        // 索引 1: 核心反查索引。給定料號 (A)，查它是 "SOURCE" 的所有規則
        @Index(name = "idx_mri_mrnb_role", columnList = "mrnb, role"),

        // 索引 2: 關聯查詢用
        @Index(name = "idx_mri_mrg_id", columnList = "mrg_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class MaterialReplacementItem {

    // --- 💖 小妤貼心常數定義 ---
    public static final String ROLE_SOURCE = "SOURCE"; // 來源 (1A + 2B)
    public static final String ROLE_TARGET = "TARGET"; // 目標 (4C + 5D)

    public MaterialReplacementItem() {
        this.syscdate = new Date();
        this.syscuser = "system";
        this.syssort = 0;
        this.sysnote = "";
    }

    // --- 系統欄位 ---
    @Column(name = "sys_c_date", nullable = false, columnDefinition = "TIMESTAMP default now()")
    private Date syscdate;

    @Column(name = "sys_c_user", nullable = false, columnDefinition = "varchar(50) default 'system'")
    private String syscuser;

    @Column(name = "sys_sort", nullable = false, columnDefinition = "int default 0")
    private Integer syssort;

    @Column(name = "sys_note", nullable = false, columnDefinition = "text default ''")
    private String sysnote;

    // --- 業務欄位 ---

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_replacement_item_seq")
    @SequenceGenerator(name = "material_replacement_item_seq", sequenceName = "material_replacement_item_seq", allocationSize = 1)
    @Column(name = "mri_id", nullable = false)
    private Long mriid;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mrg_id")
    private MaterialReplacementGroup group;

    @Column(name = "mrnb", nullable = false)
    private String mrnb; // 料號

    /**
     * 角色定義：
     * SOURCE: 構成配方的原料 (Trigger A 或 Partner B)
     * TARGET: 最終使用的替代料 (C, D)
     */
    @Column(name = "role", nullable = false)
    private String role; 

    /**
     * 數量/比例：
     * 若為 1A + 2B = 4C，則 A=1.0, B=2.0, C=4.0
     */
    @Column(name = "qty", nullable = false)
    private Double qty; 
}