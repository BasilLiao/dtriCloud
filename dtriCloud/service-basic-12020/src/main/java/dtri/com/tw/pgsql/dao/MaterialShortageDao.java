package dtri.com.tw.pgsql.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.MaterialShortage;

public interface MaterialShortageDao extends JpaRepository<MaterialShortage, Long> {

    @Modifying
    @Query(value = "TRUNCATE TABLE material_shortage RESTART IDENTITY", nativeQuery = true)
    void truncateTable();
}
