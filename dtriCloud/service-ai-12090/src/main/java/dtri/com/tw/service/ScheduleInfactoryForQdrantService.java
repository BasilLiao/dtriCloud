package dtri.com.tw.service; // 請根據你的專案路徑調整

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value; // 💡 關鍵：解決 Value 衝突的救星

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
import io.qdrant.client.grpc.JsonWithInt.Value;
import io.qdrant.client.grpc.Points.PointStruct;

@Service
public class ScheduleInfactoryForQdrantService {

	@Autowired
	private ScheduleInfactoryDao infactoryDao;

	/**
	 * 1. 執行全量同步：將 PostgreSQL 所有排程資料轉向量並存入 Qdrant
	 */
	/**
	 * 2. 僅執行封裝：將 PostgreSQL 資料封裝為 PointStruct (包含 Payload 但尚未包含向量) * @return
	 * List<PointStruct> 封裝好的點位清單
	 */
	public List<PointStruct> packagePayloads() {
		List<ScheduleInfactory> list = infactoryDao.findAll();
		List<PointStruct> packedPoints = new ArrayList<>();

		System.out.println("📦 [DTR AI] 開始封裝 Payload，目標筆數：" + list.size());

		for (ScheduleInfactory item : list) {
			try {
				// C. 封裝 Payload：將實體類別所有欄位對應進去
				Map<String, Value> payload = new HashMap<>();

				// --- 語意特徵欄位 (💡 重要：存入一個預設欄位供後續轉向量使用) ---
				String featureText = String.format("品號:%s; 品名:%s; 規格:%s; 備註:%s; 訂單:%s",item.getSinb(), item.getSipname(),
						item.getSipspecifications(), item.getSiscnote(), item.getSicorder());
				payload.put("feature_text", value(featureText));
				
				// --- 核心欄位 ---
				payload.put("si_id", value(item.getSiid()));
				payload.put("si_nb", value(item.getSinb()));
				payload.put("si_p_nb", value(item.getSipnb()));
				payload.put("si_p_name", value(item.getSipname()));
				payload.put("si_p_spec", value(item.getSipspecifications()));

				// --- 狀態與進度 ---
				payload.put("si_status", value(item.getSistatus()));
				payload.put("si_priority", value(item.getSipriority()));
				payload.put("si_sc_note", value(item.getSiscnote()));
				payload.put("si_mc_note", value(item.getSimcnote()));
				payload.put("si_mc_date", value(item.getSimcdate()));
				payload.put("si_wm_progress", value(item.getSiwmprogress()));
				payload.put("si_mp_progress", value(item.getSimpprogress()));

				// --- 客戶資訊 ---
				payload.put("si_c_order", value(item.getSicorder()));
				payload.put("si_c_note", value(item.getSicnote()));

				// D. 建立 Point (注意：此時沒有呼叫 .setVectors)
				packedPoints.add(PointStruct.newBuilder().setId(id(item.getSiid())).putAllPayload(payload).build());

			} catch (Exception e) {
				System.err.println("❌ 封裝失敗 (si_id: " + item.getSiid() + "): " + e.getMessage());
			}
		}

		System.out.println("✅ [DTR AI] 封裝完成，總計：" + packedPoints.size() + " 筆。");
		return packedPoints;
	}

}