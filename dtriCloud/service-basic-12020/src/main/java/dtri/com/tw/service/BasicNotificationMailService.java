package dtri.com.tw.service;

import java.util.ArrayList;

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
			if (toUser.length > 0) {
				// 創建 MimeMessage 物件
				MimeMessage message = mailSender.createMimeMessage();
				// 使用 MimeMessageHelper 來設置消息內容和屬性
				MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
				// 設置收件人、主題、以及內容
				helper.setTo(toUser);
				if (toCcUser.length > 0 && !toCcUser[0].equals("")) {
					helper.setCc(toCcUser);
				}
				helper.setSubject(subject);
				// 設置 HTML 格式的內容
				helper.setText(bodyHtml, true);
				// 附件?
				if (bnmattcontent != null && !bnmattname.equals("") && bnmattname != null) {
					// 使用 ByteArrayDataSource 將 byte[] 包裝成資料來源
					ByteArrayDataSource dataSource = new ByteArrayDataSource(bnmattcontent, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					helper.addAttachment(bnmattname, dataSource);

				}
				// 發送郵件
				mailSender.send(message);
			}

		} catch (Exception e) {
			System.out.println(e);
			logger.error(e.toString());
			sendOK = false;
		}
		return sendOK;
	}

	// 檢查信件
	public void readySendCheckEmail() {
		// 尚未寄信件
		ArrayList<BasicNotificationMail> mails = notificationMailDao.findAllByCheck(null, null, null, null, false, null,
				null);
		mails.forEach(m -> {
			String[] toUsers = m.getBnmmail().replace("[", "").replace("]", "").replaceAll(" ", "").split(",");
			String[] toCcUsers = m.getBnmmailcc().replace("[", "").replace("]", "").replaceAll(" ", "").split(",");

			// 傳送
			boolean ok = this.sendEmail(toUsers, toCcUsers, m.getBnmtitle(), m.getBnmcontent(), m.getBnmattname(),
					m.getBnmattcontent());
			m.setBnmsend(ok);// 成功?/失敗?
		});
		// 更新標記存入
		notificationMailDao.saveAll(mails);
	}
}
