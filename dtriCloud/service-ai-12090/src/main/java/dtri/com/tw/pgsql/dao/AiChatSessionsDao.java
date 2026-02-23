package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import dtri.com.tw.pgsql.entity.AiChatSessions;

public interface AiChatSessionsDao extends JpaRepository<AiChatSessions, Long> {

	// 修正：將 acsuid 改為 acsuaccount
	@Query("SELECT s FROM AiChatSessions s WHERE s.acsuaccount = :acsuaccount AND s.acstitle LIKE %:title%")
	ArrayList<AiChatSessions> findByTitleSearch(String acsuaccount, String title);

	// 之前的分頁查詢也要一併修正
	@Query("SELECT s FROM AiChatSessions s WHERE (:acsuaccount is null or s.acsuaccount = :acsuaccount) ORDER BY s.acscat DESC")
	ArrayList<AiChatSessions> findAllByAccount(String acsuaccount, Pageable pageable);
}