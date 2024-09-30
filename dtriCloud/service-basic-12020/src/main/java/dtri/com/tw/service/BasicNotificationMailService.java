package dtri.com.tw.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import jakarta.mail.internet.MimeMessage;

@Service
public class BasicNotificationMailService {
	@Autowired
	BasicNotificationMailDao notificationMailDao;
	@Autowired
	private JavaMailSender mailSender;

	// 寄信
	public boolean sendEmail(String[] toUser, String[] toCcUser, String subject, String bodyHtml) {
		boolean sendOK = true;
		try {
			// 簡單版mail
//			SimpleMailMessage message = new SimpleMailMessage();
//			message.setTo(to);
//			message.setSubject(subject);
//			message.setText(bodyHtml);
//			mailSender.send(message);

			// 創建 MimeMessage 物件
			MimeMessage message = mailSender.createMimeMessage();
			// 使用 MimeMessageHelper 來設置消息內容和屬性
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			// 設置收件人、主題、以及內容
			helper.setTo(toUser);
			helper.setCc(toCcUser);
			helper.setSubject(subject);
			// 設置 HTML 格式的內容
			helper.setText(bodyHtml, true);

			// 發送郵件
			mailSender.send(message);

		} catch (Exception e) {
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
			boolean ok = this.sendEmail(toUsers, toCcUsers, m.getBnmtitle(), m.getBnmcontent());
			m.setBnmsend(ok);// 成功?/失敗?
		});
		// 更新標記存入
		notificationMailDao.saveAll(mails);
	}
}
