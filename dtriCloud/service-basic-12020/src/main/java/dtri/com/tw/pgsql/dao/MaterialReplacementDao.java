package dtri.com.tw.pgsql.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.pgsql.entity.MaterialReplacement;

public interface MaterialReplacementDao extends JpaRepository<MaterialReplacement, Long> {
    // 根據品號(mrnb)查詢，用於判斷是要新增還是更新
    Optional<MaterialReplacement> findByMrnb(String mrnb);
}
