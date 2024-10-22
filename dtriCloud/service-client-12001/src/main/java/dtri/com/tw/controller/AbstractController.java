package dtri.com.tw.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;

import dtri.com.tw.login.CustomerUserDetails;
import dtri.com.tw.pgsql.entity.SystemGroup;
import dtri.com.tw.pgsql.entity.SystemPermission;

public abstract class AbstractController {

	private String sysClass;
	private String sysFunction;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 每個Level有對應的intLevel整數值，數值越小等級越高，數值越大等級越低。 Level intValue 用途<br>
	 * OFF 0 不輸出任何日誌<br>
	 * FETAL 100 造成應用程式停止的日誌<br>
	 * ERROR 200 造成應用程式錯誤的日誌<br>
	 * WARN 300 可能導致錯誤的日誌<br>
	 * INFO 400 一般資訊的日誌<br>
	 * DEBUG 500 除錯資訊的日誌<br>
	 * TRACE 600 更細的除錯資訊，通常用來追蹤程式流程的日誌<br>
	 * ALL Integer.MAX_VALUE 輸出所有日誌<br>
	 */
	public AbstractController() {
		this.sysClass = this.getClass().getSimpleName();

	}

	public void sysFunction(String sysFunction) {
		this.sysFunction = sysFunction;
		logger.info("===>>> controller : [" + sysClass + "][" + sysFunction + "] Check");
		//System.out.println("===>>> controller : [" + sysClass + "][" + sysFunction + "] Check");
	}

	// 一般提示Log
	public void loggerInf(String ms, String userAcc) {
		logger.info("===>>> [" + userAcc + "]" + ms);
		System.out.println("===>>> [" + userAcc + "]" + ms);
	}

	// 錯誤Log
	public void loggerErr(String ms, String userAcc) {
		logger.error("===>>> [" + userAcc + "]" + ms);
		System.out.println("===>>> [" + userAcc + "]" + ms);
	}

	// 異常Log
	public void loggerWarn(String ms, String userAcc) {
		logger.warn("===>>> [" + userAcc + "]" + ms);
		System.out.println("===>>> [" + userAcc + "]" + ms);
	}

	/** 轉換文字 **/
	public static String eStktToSg(Exception e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		ps.close();
		return baos.toString();
	}

	abstract String access(@RequestBody String jsonObject);

	abstract String search(@RequestBody String jsonObject);

	abstract String add(@RequestBody String jsonObject);

	abstract String modify(@RequestBody String jsonObject);

	abstract String invalid(@RequestBody String jsonObject);

	abstract String delete(@RequestBody String jsonObject);

	/** 取得<該功能> 傳送參數 */
	public void show(String msg) {
		System.out.println(msg);
	}

	/** 取得<User權限> */
	public CustomerUserDetails loginUser() {
		// 取得-當前用戶資料
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			CustomerUserDetails userDetails = (CustomerUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			// Step1.查詢資料權限
			return userDetails;
		} else {
			return null;
		}
	}

	/** 取得<UI權限> */
	public SystemPermission permissionUI() {
		// Step0.取得-當前用戶資料
		CustomerUserDetails lud = loginUser();
		if (lud != null) {
			List<SystemGroup> systemGroup = lud.getSystemGroup();
			// Step1.查詢資料權限
			SystemPermission permission = new SystemPermission();
			String sysFunction = this.sysFunction;
			systemGroup.forEach(p -> {
				if (p.getSystemPermission().getSpcontrol().equals(sysFunction)) {
					permission.setSppermission(p.getSgpermission());
				}
			});
			return permission;
		} else {
			return null;
		}
	}
}
