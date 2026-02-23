package dtri.com.tw.pgsql.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import dtri.com.tw.pgsql.entity.AiVoiceMetadata;

public interface AiVoiceMetadataDao extends JpaRepository<AiVoiceMetadata, Long> {

	// 根據關聯的訊息 ID 找語音細節
	AiVoiceMetadata findByAichatmessages_Id(Long messageId);
}