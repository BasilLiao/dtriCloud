package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicNotificationMail;

public interface BasicNotificationMailDao extends JpaRepository<BasicNotificationMail, Long> {

	@Query("SELECT c FROM BasicNotificationMail c WHERE "//
			+ "(:bnmkind is null or c.bnmkind=:bnmkind) and "//
			+ "(:bnmmail is null or c.bnmmail=:bnmmail) and "//
			+ "(:bnmcontent is null or c.bnmcontent=:bnmcontent) and " //
			+ "(:bnmtitle is null or c.bnmtitle=:bnmtitle) and  "//
			+ "(:bnmurl is null or c.bnmurl=:bnmurl) and  "//
			+ "(:bnmsend is null or c.bnmsend=:bnmsend)")
	ArrayList<BasicNotificationMail> findAllByCheck(String bnmkind, String bnmmail, String bnmcontent, String bnmtitle,
			Boolean bnmsend, String bnmurl, Pageable pageable);

	@Query("SELECT c FROM BasicNotificationMail c WHERE "//
			+ "(:bnmkind is null or c.bnmkind LIKE %:bnmkind%) and "//
			+ "(:bnmmail is null or c.bnmmail LIKE %:bnmmail%) and "//
			+ "(:bnmcontent is null or c.bnmcontent LIKE %:bnmcontent%) and " //
			+ "(:bnmtitle is null or c.bnmtitle LIKE %:bnmtitle%) and  "//
			+ "(:bnmurl is null or c.bnmurl LIKE %:bnmurl%) and "//
			+ "(:bnmsend is null or c.bnmsend=:bnmsend)")
	ArrayList<BasicNotificationMail> findAllBySearch(String bnmkind, String bnmmail, String bnmcontent, String bnmtitle,
			Boolean bnmsend, String bnmurl, Pageable pageable);

}