package dtri.com.tw.pgsql.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "ai_chat_attachments")
public class AiChatAttachments extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 對應所屬的訊息
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "message_id", nullable = false)
	@JsonBackReference
	private AiChatMessages message;

	@Column(name = "file_name")
	private String fileName;

	/**
	 * 類型：IMAGE, PDF, EXCEL, WORD...
	 */
	@Column(name = "file_type")
	private String fileType;

	/**
	 * 二進位資料
	 */
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "file_data", columnDefinition = "bytea")
	private byte[] fileData;
	
	
	/**
	 * 🚀 修正：手動處理 JSON 轉二進位邏輯
	 * 解決前端傳來包含 "data:image/png;base64," 前綴導致的解析異常
	 */
	public void setFileData(Object value) {
	    if (value == null || value.toString().isEmpty()) {
	        this.fileData = null;
	    } else if (value instanceof String) {
	        try {
	            String base64Str = (String) value;
	            // 💡 關鍵：檢查並移除 Data URL 前綴 (例如 "data:image/png;base64," 或 "image/png;base64,")
	            if (base64Str.contains(",")) {
	                base64Str = base64Str.split(",")[1];
	            }
	            this.fileData = java.util.Base64.getDecoder().decode(base64Str);
	        } catch (IllegalArgumentException e) {
	            System.err.println("附件 Base64 解碼失敗: " + e.getMessage());
	            this.fileData = null;
	        }
	    } else if (value instanceof byte[]) {
	        this.fileData = (byte[]) value;
	    }
	}
}