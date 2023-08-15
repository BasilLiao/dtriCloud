package dtri.com.tw.login;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import dtri.com.tw.pgsql.entity.SystemGroup;
import dtri.com.tw.pgsql.entity.SystemUser;

public class CustomerUserDetails implements UserDetails {
	/**
	 * 登入使用者主要資料
	 */
	private static final long serialVersionUID = 1L;
	private SystemUser user;
	private ArrayList<SystemGroup> group;
	private Collection<? extends GrantedAuthority> authorities;
	PasswordEncoder pwdEncoder;

	public CustomerUserDetails() {

	}

	public CustomerUserDetails(SystemUser user, ArrayList<SystemGroup> group, Collection<? extends GrantedAuthority> authorities) {
		super();
		this.user = user;
		this.group = group;
		this.authorities = authorities;
		pwdEncoder = new BCryptPasswordEncoder();
	}

	public ArrayList<SystemGroup> getSystemGroup() {
		return group;
	}

	public SystemUser getSystemUser() {
		return user;
	}

	/**
	 * 權限清單 <br>
	 * 規則: <br>
	 * 單元+請求類型(CRUD權限):index.basil_000100000000<br>
	 * 單元+請求類型(CRUD權限):index.basil_000010000000<br>
	 * 
	 **/
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * 注意密碼加密new BCryptPasswordEncoder().encode("user123")
	 **/
	@Override
	public String getPassword() {
		return user.getSupassword();
	}

	@Override
	public String getUsername() {
		return user.getSuaccount();
	}

	/** 用戶是否過期 **/
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/** 用戶是否鎖定還是解鎖 **/
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/** 用戶密碼是否過期 **/
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/** 用戶是否禁用 **/
	@Override
	public boolean isEnabled() {
		boolean check = (user.getSysstatus() == 0) || (user.getSysstatus() == 3);
		return check;
	}

	/** 顯示全部 **/
	@Override
	public String toString() {
		return "MyUserDetails [id=" + user.getSuid() + ", useraccount=" + user.getSuaccount() + ", password=" + user.getSupassword() + ", enabled="
				+ (user.getSysstatus() == 0) + ", authorities=" + authorities + "]";
	}
}
