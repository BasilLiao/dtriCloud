package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class BasicNotificationMailService {
	@Autowired
	BasicNotificationMailDao notificationMailDao;
	@Autowired
	private JavaMailSender mailSender;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 寄信
	public boolean sendEmail(String[] toUser, String[] toCcUser, String subject, String bodyHtml, String bnmattname,
			byte[] bnmattcontent) {
		boolean sendOK = true;
		try {
			// 簡單版mail
//			SimpleMailMessage message = new SimpleMailMessage();
//			message.setTo(to);
//			message.setSubject(subject);
//			message.setText(bodyHtml);
//			mailSender.send(message);
			if (toUser.length == 0) {
				// 沒有任何有效 TO 位址，直接返回失敗並記錄
				logger.warn("Skip sending mail: no valid TO recipients. subject={}", subject);
				return false;
			}

			// === 1) 建立 MimeMessage 與 Helper ===
			MimeMessage message = mailSender.createMimeMessage();
			// 第二個參數 multipart=true（允許 HTML + 附件），第三個參數指定 UTF-8
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			// 設置收件人、主題、以及內容
			// === 3) 設定收件人/副本 ===
			helper.setTo(toUser);
			if (toCcUser.length > 0 && !toCcUser[0].equals("")) {
				helper.setCc(toCcUser);
			}
			// === 4) 主旨/本文 null 安全處理 ===
			helper.setSubject(subject != null ? subject : "(no subject)");
			// 設置 HTML 格式的內容
			helper.setText(bodyHtml != null ? bodyHtml : "", true); // true = HTML
			// 寄出時間
			helper.setSentDate(new java.util.Date());

			// === 5) 附件（先判檔名，再判內容） ===
			if (bnmattname != null && !bnmattname.isBlank() && bnmattcontent != null && bnmattcontent.length > 0) {
				String lower = bnmattname.toLowerCase();
				String contentType = "application/octet-stream"; // 預設
				// 使用 ByteArrayDataSource 將 byte[] 包裝成資料來源
				if (lower.endsWith(".xlsx")) {
					contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
				} else if (lower.endsWith(".xls")) {
					contentType = "application/vnd.ms-excel";
				} else if (lower.endsWith(".pdf")) {
					contentType = "application/pdf";
				} else if (lower.endsWith(".csv")) {
					contentType = "text/csv";
				} else if (lower.endsWith(".txt")) {
					contentType = "text/plain; charset=UTF-8";
				}
				ByteArrayDataSource ds = new ByteArrayDataSource(bnmattcontent, contentType);
				helper.addAttachment(bnmattname, ds);
			}
			// === 7) 寄出 ===
			mailSender.send(message);

		} catch (jakarta.mail.SendFailedException e) {
			// 典型：收件人格式不合法、部份位址被拒
			sendOK = false;
			logger.error("SendFailedException: subject={}, to={}, cc={}", subject, Arrays.toString(toUser),
					Arrays.toString(toCcUser), e);

		} catch (org.springframework.mail.MailSendException e) {
			// Spring 封裝的寄送例外（底層可能是連線、驗證、IO等）
			sendOK = false;
			logger.error("MailSendException: subject={}, to={}, cc={}", subject, Arrays.toString(toUser),
					Arrays.toString(toCcUser), e);

		} catch (Exception e) {
			// 其他未預期錯誤
			sendOK = false;
			logger.error("Unexpected mail error: subject={}, to={}, cc={}", subject, Arrays.toString(toUser),
					Arrays.toString(toCcUser), e);
		}
		return sendOK;
	}

	// 檢查信件
	public void readySendCheckEmail() {
		// 尚未寄信件
		ArrayList<BasicNotificationMail> mails = notificationMailDao.findAllByCheck(null, null, null, null, false, null,
				null);
		mails.forEach(m -> {
			String[] toUsers = Arrays.stream(Optional.ofNullable(m.getBnmmail()).orElse("") // null 保護
					.replace("[", "").replace("]", "").replace("\u00A0", " ") // NBSP（非換行空白）
					.replace("\u2007", " ").replace("\u202F", " ").trim().split(",")).map(String::trim) // 去除每筆頭尾空白
					.filter(s -> !s.isEmpty()) // 過濾空字串
					.toArray(String[]::new);

			String[] toCcUsers = Arrays
					.stream(Optional.ofNullable(m.getBnmmailcc()).orElse("").replace("[", "").replace("]", "")
							.replace("\u00A0", " ").replace("\u2007", " ").replace("\u202F", " ").trim().split(","))
					.map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);

			// 傳送
			boolean ok = this.sendEmail(toUsers, toCcUsers, m.getBnmtitle(), m.getBnmcontent(), m.getBnmattname(),
					m.getBnmattcontent());
			m.setBnmsend(ok);// 成功?/失敗?
		});
		// 更新標記存入
		notificationMailDao.saveAll(mails);
	}
}
