package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dtri.com.tw.pgsql.entity.PcbConfigSettings;

/**
 * PCB 設定資料存取層 (DAO/Repository)
 */
@Repository
public interface PcbConfigSettingsDao extends JpaRepository<PcbConfigSettings, Long> {

	// 1. 基本：透過名稱精準查詢
	Optional<PcbConfigSettings> findByPcscname(String pcscname);

	// 2. 模糊查詢：用於搜尋功能 (LIKE %name%)
	ArrayList<PcbConfigSettings> findByPcscnameContainingIgnoreCase(String pcscname);

	// 3. 複合查詢：找特定層數且名稱相符的資料
	ArrayList<PcbConfigSettings> findByPcslcountAndPcscnameContaining(Integer pcslcount, String pcscname);

	// 4. 區間查詢：檔案大小在範圍內
	ArrayList<PcbConfigSettings> findByPcsfsizeBetween(Long minSize, Long maxSize);

	/**
	 * 5. 查詢：品號 / 品名 / 規格 / 廠商 / 負責RD / 設定檔名稱(備註) 使用 LIKE 模糊查詢
	 */
	@Query("SELECT p FROM PcbConfigSettings p WHERE "//
			+ "(:pcspnb IS NULL OR p.pcspnb LIKE CONCAT('%', :pcspnb, '%')) AND "//
			+ "(:pcspname IS NULL OR p.pcspname LIKE CONCAT('%', :pcspname, '%')) AND "//
			+ "(:pcspspecification IS NULL OR p.pcspspecification LIKE CONCAT('%', :pcspspecification, '%')) AND "//
			+ "(:pcspcbname IS NULL OR p.pcspcbname LIKE CONCAT('%', :pcspcbname, '%')) AND "//
			+ "(:pcsrduser IS NULL OR p.pcsrduser LIKE CONCAT('%', :pcsrduser, '%')) AND "//
			+ "(:sysnote IS NULL OR p.sysnote LIKE CONCAT('%', :sysnote, '%'))")
	ArrayList<PcbConfigSettings> findPcsBySearch(String pcspnb, String pcspname, String pcspspecification,
			String pcspcbname, String pcsrduser, String sysnote, Pageable pageable);

	/**
	 * 6. 檢查：ID / 品號 / 品名 / 規格 / 廠商 / 負責RD / 設定檔名稱(備註) 使用 = 精準比對
	 */
	@Query("SELECT p FROM PcbConfigSettings p WHERE "//
			+ "(:pcsid IS NULL OR p.pcsid = :pcsid) AND "//
			+ "(:pcspnb IS NULL OR p.pcspnb = :pcspnb) AND "//
			+ "(:pcspname IS NULL OR p.pcspname = :pcspname) AND "//
			+ "(:pcspspecification IS NULL OR p.pcspspecification = :pcspspecification) AND "//
			+ "(:pcspcbname IS NULL OR p.pcspcbname = :pcspcbname) AND "//
			+ "(:pcsrduser IS NULL OR p.pcsrduser = :pcsrduser) AND "//
			+ "(:pcscname IS NULL OR p.pcscname = :pcscname)")
	ArrayList<PcbConfigSettings> findPcsByCheck(Long pcsid, String pcspnb, String pcspname, String pcspspecification,
			String pcspcbname, String pcsrduser, String pcscname);
}