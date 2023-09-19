package dtri.com.tw.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dtri.com.tw.bean.FtpUtilBean;

@Service
public class FtpService {

	/**
	 * https://www.itread01.com/content/1541594827.html 獲取FTPClient物件
	 *
	 * @param ftpHost     FTP主機伺服器
	 * @param ftpPassword FTP 登入密碼
	 * @param ftpUserName FTP登入使用者名稱
	 * @param ftpPort     FTP埠 預設為21
	 * @return
	 */
	Logger logger = LoggerFactory.getLogger(FTPClient.class);
	@Value("${catalina.home}")
	private String apache_path;

	public static FTPClient getFTPClient(FTPClient ftpClient, String ftpHost, String ftpUserName, String ftpPassword, int ftpPort) {
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(ftpHost, ftpPort);// 連線FTP伺服器
			ftpClient.login(ftpUserName, ftpPassword);// 登陸FTP伺服器
			if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				System.out.println("未連線到FTP，使用者名稱或密碼錯誤。");
				ftpClient.disconnect();
			} else {
				// 設定檔案傳輸型別為二進位制+UTF-8 傳輸
				ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
				ftpClient.setControlEncoding("UTF-8");
				// 獲取ftp登入應答程式碼
				int reply = ftpClient.getReplyCode();
				// 驗證是否登陸成功
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftpClient.disconnect();
					System.err.println("FTP server refused connection.");
					return null;
				}
				System.out.println("FTP連線成功。");
			}
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("FTP的IP地址可能錯誤，請正確配置。");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("FTP的埠錯誤,請正確配置。");
		}
		return ftpClient;
	}

	/***
	 * 從FTP伺服器下載檔案
	 * 
	 * @param ftpHost     FTP IP地址
	 * @param ftpUserName FTP 使用者名稱
	 * @param ftpPassword FTP使用者名稱密碼
	 * @param ftpPort     FTP埠
	 * @param ftpPath     FTP伺服器中檔案所在路徑 格式： ftptest/aa
	 * @param localPath   下載到本地的位置 格式：H:/download
	 * @param fileName    檔名稱
	 */
	public boolean downloadFtpFile(FtpUtilBean ftp) {
		boolean success = false;
		FTPClient ftpClient = new FTPClient();

		try {
			ftpClient = getFTPClient(ftpClient, ftp.getFtpHost(), ftp.getFtpUserName(), ftp.getFtpPassword(), ftp.getFtpPort());
			ftpClient.setControlEncoding("UTF-8"); // 中文支援
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftp.getFtpPath());
			// Step3-1.查資料夾
			File directory = new File(apache_path + ftp.getLocalPath());
			if (!directory.exists()) {
				directory.mkdir();
			}
			// Step3-2.建立檔案
			File localFile = new File(apache_path + ftp.getLocalPath() + File.separatorChar + ftp.getFileName());
			OutputStream os = new FileOutputStream(localFile);
			// System.out.println(ftp.getRemotePath()+"/"+ftp.getFileName());
			ftpClient.retrieveFile(ftp.getRemotePath() + "/" + ftp.getFileName(), os);
			// System.out.println(ftpClient.getReplyCode());
			os.close();
			ftpClient.logout();
			success = true;
		} catch (FileNotFoundException e) {
			System.out.println("沒有找到" + ftp.getFtpPath() + "檔案");
			e.printStackTrace();
			return success;
		} catch (SocketException e) {
			System.out.println("連線FTP失敗.");
			e.printStackTrace();
			return success;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("檔案讀取錯誤。");
			e.printStackTrace();
			return success;
		}
		return success;
	}

	/**
	 * Description: 向FTP伺服器上傳檔案
	 * 
	 * @param ftpHost     FTP伺服器hostname
	 * @param ftpUserName 賬號
	 * @param ftpPassword 密碼
	 * @param ftpPort     埠
	 * @param ftpPath     FTP伺服器中檔案所在路徑 格式： ftptest/aa
	 * @param fileName    ftp檔名稱
	 * @param input       檔案流
	 * @return 成功返回true，否則返回false
	 */
	public boolean uploadFile(FtpUtilBean ftp) {
		boolean success = false;
		FTPClient ftpClient = new FTPClient();
		try {
			int reply;
			ftpClient = getFTPClient(ftpClient, ftp.getFtpHost(), ftp.getFtpUserName(), ftp.getFtpPassword(), ftp.getFtpPort());
			reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftpClient.disconnect();
				return success;
			}
			ftpClient.setControlEncoding("UTF-8"); // 中文支援
			ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftp.getFtpPath());
			// 上傳資料位置
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(ftp.getLocalPath()));
			System.out.println(ftp.getRemotePathBackup());
			ftpClient.storeFile(ftp.getRemotePathBackup(), inputStream);
			inputStream.close();
			// ftp.getInput().close();
			ftpClient.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
			return success;
		} finally {
			if (ftpClient.isConnected()) {
				try {
					ftpClient.disconnect();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		return success;
	}

	/**
	 * Creates a nested directory structure on a FTP server 創建資料夾
	 * 
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param dirPath   Path of the directory, i.e /projects/java/ftp/demo
	 * @return true if the directory was created successfully, false otherwise
	 * @throws IOException if any error occurred during client-server communication
	 */
	public static boolean makeDirectories(FTPClient ftpClient, String dirPath) throws IOException {
		boolean created = ftpClient.makeDirectory(dirPath);

		return created;
	}
}