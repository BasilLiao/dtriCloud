package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import dtri.com.tw.pgsql.entity.AiChatMessages;

public interface AiChatMessagesDao extends JpaRepository<AiChatMessages, Long> {

	// 核心動作：撈取某個會話的所有對話紀錄，並確保順序正確
	// 我們使用 Entity 裡定義的 acmcat 排序，或者用 ID 排序亦可
	@Query("SELECT m FROM AiChatMessages m WHERE m.acmsessions.acsid = :acsid " + "ORDER BY m.acmcat ASC")
	ArrayList<AiChatMessages> findAllBySessionId(Long acsid);

	// 搜尋某會話中是否包含特定關鍵字的訊息 (例如：搜尋特定的工單編號)
	@Query("SELECT m FROM AiChatMessages m WHERE m.acmsessions.acsid = :acsid " + "AND m.acmcontent LIKE %:keyword%")
	ArrayList<AiChatMessages> findKeywordInSession(Long acsid, String keyword);

	// 找出所有帶有 Excel 下載網址的訊息
	@Query("SELECT m FROM AiChatMessages m WHERE m.acmmtype = :acmmtype AND m.acmaurl IS NOT NULL AND m.acmaurl != ''")
	ArrayList<AiChatMessages> findValidFiles(String acmmtype);
}