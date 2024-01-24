package dtri.com.tw.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;

@Service
public class BasicNotificationMailService {
	@Autowired
	BasicNotificationMailDao notificationMailDao;
	@Autowired
	private JavaMailSender mailSender;

	// 寄信
	public boolean sendEmail(String[] to, String subject, String body) {
		boolean sendOK = true;
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
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
			boolean ok = this.sendEmail(toUsers, m.getBnmtitle(), m.getBnmcontent());
			m.setBnmsend(ok);// 成功?/失敗?
		});
		// 更新標記存入
		notificationMailDao.saveAll(mails);
	}
}
