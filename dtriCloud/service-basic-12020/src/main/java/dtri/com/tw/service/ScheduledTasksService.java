package dtri.com.tw.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import dtri.com.tw.bean.FtpUtilBean;
import dtri.com.tw.pgsql.dao.SystemConfigDao;
import dtri.com.tw.pgsql.entity.SystemConfig;
import dtri.com.tw.shared.CloudExceptionService;

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
	private static boolean fixDelay_ERPSynchronizeServiceRun = true;
	@Autowired
	private SystemConfigDao sysDao;
	@Value("${catalina.home}")
	private String apache_path;

	@Autowired
	SynchronizeERPService synchronizeERPService;

	@Autowired
	SynchronizeBiosService synchronizeBiosService;

	@Autowired
	SynchronizeBomService synchronizeBomService;

	@Autowired
	SynchronizeScheduledService synchronizeScheduledService;

	@Autowired
	BasicNotificationMailService mailService;

	// fixedDelay = 60000 表示當前方法執行完畢 60000ms(1分鐘) 後，Spring scheduling會再次呼叫該方法
	@Scheduled(fixedDelay = 120000)
	@Async
	public void fixDelay_SynchronizeERPAutoService() {
		logger.info("===ERP_fixedRate: 時間:{}", dateFormat.format(new Date()));
		// ============ 物料+儲位同步 ============
		if (fixDelay_ERPSynchronizeServiceRun) {
			fixDelay_ERPSynchronizeServiceRun = false;
			try {
				System.out.println(new Date());
				// 初始化
				synchronizeERPService.erpSynchronizeInvtb();//
				// 事先準備匹配
				synchronizeERPService.initERPSynchronizeService();//
				// 單據
				synchronizeERPService.erpSynchronizeInvta();
				synchronizeERPService.erpSynchronizeInvtg();
				synchronizeERPService.erpSynchronizeInvth();
				synchronizeERPService.erpSynchronizeMocta();
				synchronizeERPService.erpSynchronizeMocte();
				synchronizeERPService.erpSynchronizeMoctf();
				synchronizeERPService.erpSynchronizeMocth();
				synchronizeERPService.erpSynchronizeBomtd();
				synchronizeERPService.erpSynchronizeBomtf();
				synchronizeERPService.erpSynchronizeCopth();
				synchronizeERPService.erpSynchronizePurth();
				synchronizeERPService.erpSynchronizeWtypeFilter();
				// 移除多於資料()
				synchronizeERPService.remove120DayData();
				// ==================生管機制==================
				// 外包生管排程(同步)
				synchronizeScheduledService.erpSynchronizeScheduleOutsourcer();
				// 缺料通知
				synchronizeScheduledService.scheduleShortageNotification();
				// 生管排程寄信通知(測試用)
				//synchronizeScheduledService.scheduleOutNotification();
				// ==================產品BOM==================
				// BOM機種別
				synchronizeBomService.erpSynchronizeProductModel();
				// BOM結構同步
				synchronizeBomService.erpSynchronizeBomIngredients();
				// BOM 規則同步
				synchronizeBomService.autoBISF();
				// ==================產品BIOS==================
				// BIOS檢查機種別
				synchronizeBiosService.erpSynchronizeProductModeltoBios();

			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("===>>> [System or User]" + CloudExceptionService.eStktToSg(e));
				fixDelay_ERPSynchronizeServiceRun = true;
			}
			fixDelay_ERPSynchronizeServiceRun = true;
			System.out.println(new Date());
		}
	}

	// 每日(30)07:30分執行一次
	// 自動同步(ERP產品抓取for BIOS)
	@Scheduled(cron = "0 30 07 * * ? ")
	public void updateEveryday() {
		try {
			// BIOS檢查版本
			synchronizeBiosService.versionCheckBios();
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("===>>> [System or User]" + CloudExceptionService.eStktToSg(e));
		}
	}

	// 每周一(07:30)執行一次
	@Scheduled(cron = "0 30 07 ? * MON")
	public void updateEveryMonday() {
		try {
			// 生管排程寄信通知
			synchronizeScheduledService.scheduleOutNotification();
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("===>>> [System or User]" + CloudExceptionService.eStktToSg(e));
		}
	}

	// 每日(3分鐘)-自動同步(Mail Auto 檢查)
	@Async
	@Scheduled(fixedDelay = 180000)
	public void fixDelay_MailAutoService() {
		logger.info("===Mail_fixedRate: 時間:{}", dateFormat.format(new Date()));
		try {
			// 檢查信件 寄信
			mailService.readySendCheckEmail();
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("===>>> [System or User]" + CloudExceptionService.eStktToSg(e));
		}
	}

	// 每日(30)12/22:00分執行一次
	// 系統 備份(pgsql+ftp)
	@Async
	@Scheduled(cron = "0 30 12,22 * * ? ")
	public void backupDataBase() {
		System.out.println("每隔1天 早上12點30分/晚上18點30 執行一次：" + new Date());
		logger.info("Database backup night 18.30  執行一次：" + new Date());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String backupDay = sdf.format(new Date());
		System.out.println("備份資料庫:" + new Date());
		logger.info("備份資料庫：" + new Date());

		// Step1. 備份位置
		ArrayList<SystemConfig> ftp_config = sysDao.findAllByConfig(null, "FTP_DATA_BKUP", null, null, 0, false, null);
		JsonObject c_json = new JsonObject();
		ftp_config.forEach(c -> {
			c_json.addProperty(c.getScname(), c.getScvalue());
		});
		String ftp_host = c_json.get("IP").getAsString(), //
				ftp_user_name = c_json.get("ACCOUNT").getAsString(), //
				ftp_password = c_json.get("PASSWORD").getAsString(), //
				ftp_remote_path = c_json.get("PATH").getAsString();//
		int ftp_port = c_json.get("FTP_PORT").getAsInt();

		// Step2. 資料庫設定
		ArrayList<SystemConfig> data_config = sysDao.findAllByConfig(null, "DATA_BKUP", null, null, 0, false, null);
		JsonObject d_json = new JsonObject();
		data_config.forEach(d -> {
			d_json.addProperty(d.getScname(), d.getScvalue());
		});
		String db_folder_name = d_json.get("FOLDER_NAME").getAsString(), //
				db_file_name = d_json.get("FILE_NAME").getAsString(), //
				db_pg_dump = d_json.get("PG_DUMP").getAsString(), //
				db_name = d_json.get("DB_NAME").getAsString();//
		int db_port = d_json.get("DB_PORT").getAsInt();

		// Runtime rt = Runtime.getRuntime();
		// rt = Runtime.getRuntime();
		// Step3. 備份指令-postgres
		Process p;
		/**
		 * Apache C:\Users\Basil\AppData\Local\Temp\tomcat
		 * 
		 * C:\Program Files\PostgreSQL\10\bin\pg_dump.exe --file
		 * "C:\\Users\\Basil\\Desktop\\DTRIME~1.SQL" --host "localhost" --port "5432"
		 * --username "postgres" --no-password --verbose --format=c --blobs --encoding
		 * "UTF8" "dtrimes"
		 */

		ProcessBuilder pb = new ProcessBuilder("" + db_pg_dump, "--dbname=" + db_name, "--port=" + db_port,
				"--no-password", "--verbose", "--format=c", "--blobs", "--encoding=UTF8",
				"--file=" + apache_path + db_folder_name + db_file_name + "_" + backupDay + ".sql");
		try {
			// Step3-1.查資料夾
			File directory = new File(apache_path + db_folder_name);
			if (!directory.exists()) {
				directory.mkdir();
			}

			p = pb.start();
			final BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = r.readLine();
			while (line != null) {
				System.err.println(line);
				logger.info(line);
				line = r.readLine();
			}
			r.close();
			p.waitFor();
			System.out.println(p.exitValue());
			logger.info(p.exitValue() + "");
		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
		// Step4. 上傳-FTP
		try {
			// File initialFile = new File(apache_path + db_folder_name + db_file_name + "_"
			// + backupDay + ".sql");
			// InputStream input = new FileInputStream(initialFile);
			FtpUtilBean f_Bean = new FtpUtilBean(ftp_host, ftp_user_name, ftp_password, ftp_port);
			f_Bean.setLocalPath(apache_path + db_folder_name + db_file_name + "_" + backupDay + ".sql");
			f_Bean.setRemotePathBackup(ftp_remote_path + db_file_name + "_" + backupDay + ".sql");
			f_Bean.setFileName(db_file_name + "_" + backupDay + ".sql");
			FtpService fts = new FtpService();
			fts.uploadFile(f_Bean);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/** 同步中? **/
	public synchronized static boolean isFixDelay_ERPSynchronizeServiceRun() {
		return fixDelay_ERPSynchronizeServiceRun;
	}

	// 手動比對單據
	public synchronized void fixDelay_ERPSynchronizeService() {
		logger.info("===fixedRate: 時間:{}", dateFormat.format(new Date()));
		// ============ 物料+儲位同步 ============
		if (fixDelay_ERPSynchronizeServiceRun) {
			fixDelay_ERPSynchronizeServiceRun = false;
			try {
				// 事先準備匹配
				synchronizeERPService.initERPSynchronizeService();//
				// 單據
				// ============ A111 費用領料單 / A112 費用退料單 / A119 料號調整單 / A121 倉庫調撥單 ============
				// synchronizeService.erpSynchronizeInvta();
				// ============ A131 庫存借出單 / A141 庫存借入單 ============
				// synchronizeService.erpSynchronizeInvtg();
				// ============ 借出歸還A151 / 借入歸還單A161 ============
				// synchronizeService.erpSynchronizeInvth();

				// ============ A541 廠內領料單 / A542 補料單 /(A543 超領單)/ A551 委外領料單 / A561 廠內退料單 /
				// A571
				synchronizeERPService.erpSynchronizeMocte();
				// ============A581 生產入庫單 ============
				// synchronizeService.erpSynchronizeMoctf();
				// ============ A591 委外進貨單 ============
				// synchronizeService.erpSynchronizeMocth();
				// ============ 組合單 / A421 ============
				// synchronizeService.erpSynchronizeBomtd();
				// ============ OK 拆解單 / A431 ============
				// synchronizeService.erpSynchronizeBomtf();
				// ============ A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單 ============
				// synchronizeService.erpSynchronizePurth();

			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("===>>> [System or User]" + CloudExceptionService.eStktToSg(e));
				fixDelay_ERPSynchronizeServiceRun = true;
			}
			fixDelay_ERPSynchronizeServiceRun = true;
		}
	}

//	// fixedRate = 60000 表示當前方法開始執行 60000ms(1分鐘) 後，Spring scheduling會再次呼叫該方法
//	@Scheduled(fixedRate = 60000)
//	public void testFixedRate() {
//		logger.info("===fixedRate: 時間:{}", dateFormat.format(new Date()));
//	}
//
//	// initialDelay = 180000 表示延遲 180000 (3秒) 執行第一次任務, 然後每 5000ms(5 秒) 再次呼叫該方法
//	@Scheduled(initialDelay = 180000, fixedRate = 5000)
//	public void testInitialDelay() {
//		logger.info("===initialDelay: 時間:{}", dateFormat.format(new Date()));
//	}
//
//	// cron接受cron表示式，根據cron表示式確定定時規則
//	@Scheduled(cron = "0 0/1 * * * ?")
//	public void testCron() {
//		logger.info("===cron: 時間:{}", dateFormat.format(new Date()));
//	}

}
