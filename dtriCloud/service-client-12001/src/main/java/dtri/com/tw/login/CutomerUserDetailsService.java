package dtri.com.tw.login;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.pgsql.dao.SystemUserDao;
import lombok.RequiredArgsConstructor;

//https://github.com/Ons-diweni/Spring-Security-6-JWT
// ---登入時-驗證---[Spring 自動呼叫]
@Component
@RequiredArgsConstructor
public class CutomerUserDetailsService implements UserDetailsService {

	@Autowired
	private SystemUserDao userDao;

	@Override
	public UserDetails loadUserByUsername(String userAccount) throws UsernameNotFoundException {
		// 準備
		Set<SystemGroup> systemGroups = new HashSet<SystemGroup>();
		List<GrantedAuthority> authorities = new ArrayList<>();
		List<SystemUser> users = new ArrayList<SystemUser>();
		SystemUser user = new SystemUser();

		// Step1.是否有-使用者
		users = userDao.findAllBySystemUser(null, userAccount, null, 4, null);
		if (users == null || users.size() != 1) {
			throw new UsernameNotFoundException("Can't get all user!!");
		}

		// Step2.是否有-權限清單
		user = users.get(0);
		systemGroups = user.getSystemgroups();
		if (systemGroups == null || systemGroups.size() == 0) {
			throw new UsernameNotFoundException("Can't get all group!!");
		}

		// Step3.取得-權限清單
		for (SystemGroup systemGroup : systemGroups) {
			systemGroup.getSystemPermission().getSpcontrol().replaceAll("\\.", "_");// index.basil=>index_basil
			systemGroup.getSystemPermission().getSppermission();// 000000000001
			// 將每一格 權限分析
			char[] ch = systemGroup.getSgpermission().toCharArray();
			for (int i = 11; i >= 0; i--) {
				// 如果有權限->建立通過
				if (ch[i] == '1') {
					int move_p = 11 - i;
					double now_p = Math.pow(10, move_p);
					String now_s = String.format("%012d", (int) now_p);
					String role = systemGroup.getSystemPermission().getSpcontrol().replaceAll("\\.", "_") //
							+ "_"//
							+ now_s;
					authorities.add(new SimpleGrantedAuthority(role));
				}
			}
		}

		// 是否有-使用者-TEST
		// user.setSuaccount("admin");
		// user.setSupassword("$2a$10$1aTotRT77Ckuw0QjmFTmJ.Ar4v03HoFsZaFlJTtYG8dWAPN2V6U3O");
		// user.setSupassword(new BCryptPasswordEncoder().encode("dtrcloud@123"));// 加密
		// user.setSysstatus(0);
		// 取得-權限清單-TEST
		// role = "index_basil_000000000001";
		// authorities.add(new SimpleGrantedAuthority(role));

		// Step4.存入Spring Security專用 使用者物件內
		UserDetails userDetail = new CustomerUserDetails(user, new ArrayList<SystemGroup>(systemGroups), authorities);
		return userDetail;
	}
}