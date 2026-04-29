package dtri.com.tw.mssql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "INVMB")
@EntityListeners(AuditingEntityListener.class)
public class Invmb {

    @Id
    @Column(name = "INVMB_ID")
    private String invmbid;
}
