package dtri.com.tw.login;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import dtri.com.tw.db.entity.SystemGroup;
import dtri.com.tw.db.entity.SystemUser;
import lombok.RequiredArgsConstructor;

//https://github.com/Ons-diweni/Spring-Security-6-JWT
// ---登入時-驗證---[Spring 自動呼叫]
@Component
@RequiredArgsConstructor
public class CutomerUserDetailsService implements UserDetailsService {
	@Override
	public UserDetails loadUserByUsername(String userAccount) throws UsernameNotFoundException {

		// 準備
		List<SystemGroup> systemGroups = new ArrayList<SystemGroup>();
		List<GrantedAuthority> authorities = new ArrayList<>();
		List<SystemUser> users = new ArrayList<SystemUser>();
		String role = "";

//		// Step1.是否有-使用者
//		if (users == null || users.size() != 1) {
//			throw new UsernameNotFoundException("Can't get all user!!");
//		}

//		// Step2.是否有-權限清單
//		if (systemGroups == null || systemGroups.size() == 0) {
//			throw new UsernameNotFoundException("Can't get all group!!");
//		}
//		// Step3.取得-權限清單
//		for (SystemGroup systemGroup : systemGroups) {
//			role = "";
//			systemGroup.getSystemPermission().getSpcontrol().replaceAll("\\.", "_");// index.basil=>index_basil
//			systemGroup.getSystemPermission().getSppermission();// 000000000001
//			role = systemGroup.getSystemPermission().getSpcontrol().replaceAll("\\.", "_") //
//					+ "_"//
//					+ systemGroup.getSystemPermission().getSppermission();
//			authorities.add(new SimpleGrantedAuthority(role));
//		}

		// 是否有-使用者-TEST
		SystemUser user = new SystemUser();
		user.setSuaccount("user");
		user.setSupassword(new BCryptPasswordEncoder().encode("user123"));// 加密
		user.setSysstatus(0);
		// 取得-權限清單-TEST
		role = "index_basil_000000000001";
		authorities.add(new SimpleGrantedAuthority(role));
		
		// Step4.存入Spring Security專用 使用者物件內
		UserDetails userDetail = new CustomerUserDetails(user, systemGroups, authorities);
		return userDetail;
	}
}