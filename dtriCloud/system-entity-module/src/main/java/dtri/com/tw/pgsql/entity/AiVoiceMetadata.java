package dtri.com.tw.pgsql.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Basil
 * @description 語音元數據實體 (Voice Metadata Entity)
 * 專門存放語音轉文字 (STT) 或文字轉語音 (TTS) 的技術參數。
 * 此類別透過 One-to-One 關聯附屬於 AiChatMessages，將「聲音」屬性與「文字內容」完美分離。
 */
@Getter
@Setter
@Entity
@Table(name = "ai_voice_metadata")
@EntityListeners(AuditingEntityListener.class)
public class AiVoiceMetadata extends BaseEntity {

    /**
     * 無參建構子：符合 JPA 反射機制，並進行安全初始化。
     * super() 確保父類別 BaseEntity 的系統追蹤欄位 (如創建時間) 被觸發。
     */
    public AiVoiceMetadata() {
        super(); 
        this.setAvmttext("");     // 初始為空字串，避免 STT 失敗時出現 null
        this.setAvmduration(0.0); // 時長初始為 0
        this.setAvmvmodel("");    // 初始模型名稱為空
        this.setAvmsrate(0);      // 初始取樣率
    }

    /**
     * 唯一識別碼 (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 一對一關聯 (與訊息主體連結)：
     * avm_id: 外鍵欄位，指向 ai_chat_messages 的主鍵。
     * FetchType.LAZY: 延遲載入。當我們只看文字列表時，不需要載入沈重的語音元數據，節省內存。
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avm_id")
    private AiChatMessages aichatmessages;

    /**
     * 語音轉文字的內容：
     * 作用：存放 STT 工具（如 OpenAI Whisper）解析出的原始文本。
     * 實務：可以用來與 AI 修正後的文本進行比對，檢查語音辨識的正確率。
     */
    @Column(name = "avm_t_text", nullable = false, columnDefinition = "text default ''")
    private String avmttext; 

    /**
     * 音檔時長 (秒)：
     * 作用：前端 UI 顯示（例如顯示 [00:15]）。
     * 實務：用於統計資源消耗或判定該語音是否過短（例如誤觸錄音）。
     * Double Precision: 確保能紀錄到微秒級的精確度。
     */
    @Column(name = "avm_duration", nullable = false, columnDefinition = "double precision default 0.0")
    private Double avmduration; 

    /**
     * 採用的語音模型：
     * 作用：標註是誰生成的聲音或誰辨識的文字。
     * 範例：OpenAI Whisper-v3 (STT), Azure Neural TTS (TTS)。
     * 實務：未來模型升級時，可追蹤舊資料是用哪個舊版本處理的。
     */
    @Column(name = "avm_v_model", nullable = false, columnDefinition = "varchar(50)")
    private String avmvmodel; 

    /**
     * 音訊取樣率 (Sample Rate)：
     * 8,000 Hz: 電訊等級，品質較低。
     * 16,000 Hz: 語音 AI 的黃金標準（STT 最愛）。
     * 44,100/48,000 Hz: 高保真音樂/影片等級。
     * 作用：後端處理音訊流 (Stream) 時，必須知道取樣率才能正確還原波形。
     */
    @Column(name = "avm_s_rate", nullable = false, columnDefinition = "int default 0")
    private Integer avmsrate; 

}
