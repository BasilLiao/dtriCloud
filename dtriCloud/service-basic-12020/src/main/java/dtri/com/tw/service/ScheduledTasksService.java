package dtri.com.tw.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import dtri.com.tw.mssql.dao.MoctaDao;
import dtri.com.tw.mssql.entity.Mocta;
import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.shared.Fm_T;

/***
 * https://polinwei.com/spring-boot-scheduling-tasks/ 排程 cron:
 * cron表示式，指定任務在特定時間執行；<br>
 * fixedDelay: 表示上一次任務執行完成後多久再次執行，引數型別為long，單位ms；<br>
 * fixedDelayString: 與fixedDelay含義一樣，只是引數型別變為String；<br>
 * fixedRate: 表示按一定的頻率執行任務，引數型別為long，單位ms；<br>
 * fixedRateString: 與fixedRate的含義一樣，只是將引數型別變為String；<br>
 * initialDelay: 表示延遲多久再第一次執行任務，引數型別為long，單位ms；<br>
 * initialDelayString: 與initialDelay的含義一樣，只是將引數型別變為String；<br>
 * zone: 時區，預設為當前時區，一般沒有用到<br>
 **/
@Component
public class ScheduledTasksService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	MoctaDao moctaDao;
	@Autowired
	BasicCommandListDao commandListDao;

	// fixedDelay = 60000 表示當前方法執行完畢 60000ms(10分鐘) 後，Spring scheduling會再次呼叫該方法
	@Scheduled(fixedDelay = 600000)
	public void testFixDelay() {
		logger.info("===fixedDelay: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Mocta> moctas = moctaDao.findAllByMocta();
		ArrayList<BasicCommandList> commandLists = new ArrayList<BasicCommandList>();
		// 轉換
		moctas.forEach(m -> {
			BasicCommandList c = new BasicCommandList();

			// 基本:工單+物料
			String bclclass = m.getTa001_ta002().split("-")[0];
			String bclsn = m.getTa001_ta002().split("-")[1];
			String bclpnumber = m.getMb001();
			String checkSum = m.toString().replaceAll("\\s", "");
			Boolean checkUpdate = true;
			// 檢查[Cloud] 是否有此製令單+物料
			ArrayList<BasicCommandList> entityOld = commandListDao.findAllByComList(bclclass, bclsn, bclpnumber, null);
			if (entityOld.size() != 0) {
				// 檢查是否異動?
				String entityOldSum = entityOld.get(0).getChecksum();
				if (!entityOldSum.equals(checkSum)) {
					c = entityOld.get(0);
				} else {
					checkUpdate = false;
				}
			}

			// 是否需要[更新 or 新增]?
			if (checkUpdate) {
				c.setChecksum(checkSum);
				c.setBclproduct(m.getTa006());
				c.setBclfromcommand("[" + m.getTa026_ta027_ta028() + "_訂單" + "]");// 訂單
				c.setBclclass(m.getTa001_ta002().split("-")[0]);// 製令單[別]
				c.setBclsn(m.getTa001_ta002().split("-")[1]);// 製令單[號]
				c.setBcltype(m.getTk000());// 製令單
				c.setBclcheckin(1);// 0=未核單 1=已核單
				c.setBclacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				c.setBclpnumber(m.getMb001());// 物料號品號
				c.setBclpname(m.getMb002());// 品名
				c.setBclpspecification(m.getMb003());// 規格
				c.setBclpnqty(m.getTb004());// 需領用
				c.setBcltocommand("[]");// 單據指令對象
				c.setBcltowho("[]");
				c.setBclfromwho(m.getMb017() + "_" + m.getMc002());// 倉別代號+倉別名稱
				c.setBcledate(Fm_T.toYMDate(m.getTb015()));// 預計領料日

				m.getTa009();// 預計開工日
				m.getTa010();// 預計完工日
				m.getTb005();// 已領用
				m.getMb032();// 供應商
				m.getMa002();// 供應商名稱
				commandLists.add(c);
			}
		});
		commandListDao.saveAll(commandLists);
	}

	// fixedRate = 60000 表示當前方法開始執行 60000ms(1分鐘) 後，Spring scheduling會再次呼叫該方法
	@Scheduled(fixedRate = 60000)
	public void testFixedRate() {
		logger.info("===fixedRate: 時間:{}", dateFormat.format(new Date()));
	}

	// initialDelay = 180000 表示延遲 180000 (3秒) 執行第一次任務, 然後每 5000ms(5 秒) 再次呼叫該方法
	@Scheduled(initialDelay = 180000, fixedRate = 5000)
	public void testInitialDelay() {
		logger.info("===initialDelay: 時間:{}", dateFormat.format(new Date()));
	}

	// cron接受cron表示式，根據cron表示式確定定時規則
	@Scheduled(cron = "0 0/1 * * * ?")
	public void testCron() {
		logger.info("===cron: 時間:{}", dateFormat.format(new Date()));
	}

}
