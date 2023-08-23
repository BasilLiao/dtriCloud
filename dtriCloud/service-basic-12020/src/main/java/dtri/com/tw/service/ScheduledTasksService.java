package dtri.com.tw.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
	ERPSynchronizeService synchronizeService;

	// fixedDelay = 60000 表示當前方法執行完畢 60000ms(1分鐘) 後，Spring scheduling會再次呼叫該方法
	@Scheduled(fixedDelay = 60000)
	public void fixDelay_ERPSynchronizeService() {
		logger.info("===fixedRate: 時間:{}", dateFormat.format(new Date()));
		
		synchronizeService.erpSynchronizeInvta();
		synchronizeService.erpSynchronizeInvtb();
		synchronizeService.erpSynchronizeInvtg();
		synchronizeService.erpSynchronizeInvth();

		synchronizeService.erpSynchronizeMocta();
		synchronizeService.erpSynchronizeMocte();
		synchronizeService.erpSynchronizeMoctf();
		synchronizeService.erpSynchronizeMocth();

		synchronizeService.erpSynchronizePurth();
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
