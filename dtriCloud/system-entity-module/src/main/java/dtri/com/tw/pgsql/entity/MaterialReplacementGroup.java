package dtri.com.tw.pgsql.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * 替代群組主檔 (Header)
 * 支援 N對N 邏輯：一個 Group 內包含多個 SOURCE (配方原料) 與多個 TARGET (替代結果)
 */
@Entity
@Table(name = "material_replacement_group", indexes = {
        // 索引: 快速篩選 Scope (例如: 找 客戶=Dell 且 未刪除 的規則)
        @Index(name = "idx_mrg_scope_status", columnList = "scope_type, scope_val, sys_status"),
        // 索引: 用單號查詢
        @Index(name = "idx_mrg_nb", columnList = "mrg_nb")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class MaterialReplacementGroup {

    public MaterialReplacementGroup() {
        this.syscdate = new Date();
        this.syscuser = "system";
        this.sysmdate = new Date();
        this.sysmuser = "system";
        this.sysodate = new Date();
        this.sysouser = "system";

        this.sysheader = false;
        this.sysstatus = 0;
        this.syssort = 0;
        this.sysnote = "";
    }

    // --- 系統欄位 (保持原樣) ---
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
    // ---------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_replacement_group_seq")
    @SequenceGenerator(name = "material_replacement_group_seq", sequenceName = "material_replacement_group_seq", allocationSize = 1)
    @Column(name = "mrg_id", nullable = false)
    private Long mrgid;

    @Column(name = "mrg_nb", nullable = false)
    private String mrgnb; // 規則編號/名稱

    @Column(name = "scope_type", nullable = false)
    private Integer scopetype; // 0:通用, 1:客戶指定, 2:產品指定

    @Column(name = "scope_val", nullable = false)
    private String scopeval; // 客戶代號 或 產品代號

    @Column(name = "policy", nullable = false)
    private String policy; // 策略備註

    // CascadeType.ALL + orphanRemoval = true 確保子表共存亡
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MaterialReplacementItem> items = new ArrayList<>();

    // --- 💖 小妤貼心 Helper Methods ---

    public void addItem(MaterialReplacementItem item) {
        items.add(item);
        item.setGroup(this);
    }

    public void removeItem(MaterialReplacementItem item) {
        items.remove(item);
        item.setGroup(null);
    }

    /**
     * 取得所有的來源料 (包含主觸發 A 與 隊友 B)
     * role = "SOURCE"
     */
    @JsonIgnore
    @Transient // 不對應 DB 欄位，純 Java 邏輯
    public List<MaterialReplacementItem> getSourceItems() {
        return items.stream()
                .filter(i -> "SOURCE".equals(i.getRole()))
                .collect(Collectors.toList());
    }

    /**
     * 取得所有的目標料 (消耗 C, D)
     * role = "TARGET"
     */
    @JsonIgnore
    @Transient
    public List<MaterialReplacementItem> getTargetItems() {
        return items.stream()
                .filter(i -> "TARGET".equals(i.getRole()))
                .collect(Collectors.toList());
    }
}