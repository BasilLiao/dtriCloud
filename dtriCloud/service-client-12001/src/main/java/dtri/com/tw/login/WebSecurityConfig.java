package dtri.com.tw.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
	@Autowired
	JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	CutomerUserDetailsService customerUserDetailsService;

	/**
	 * (1)It is used for storing a password <br>
	 * that needs to be compared to the user-provided password <br>
	 * at the time of authentication.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean // (2)It defines how Spring Security Filters perform authentication.
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	// 可訪問清單:Restful API (GET -> 訪問/查詢) (POST -> 新增) (PUT -> 更新) (DELETE -> 刪除)
	private static final String system_con = "/ajax/system_config.basil";
	private static final String system_per = "/ajax/system_permission.basil";
	private static final String system_gro = "/ajax/system_group.basil";
	private static final String system_use = "/ajax/system_user.basil";

	/**
	 * 這個method可以設定那些路由要經過身分權限的審核，或是login、logout路由特別設定等地方，因此這邊也是設定身分權限的關鍵地方。<br>
	 * authorizeHttpRequests()配置路徑攔截，表明路徑訪問所對應的權限，角色，認證信息。<br>
	 * formLogin()對應表單認證相關的配置<br>
	 * logout()對應了註銷相關的配置<br>
	 * httpBasic()可以配置basic登錄<br>
	 * (permitAll = 全部允許) (authenticated = 登入後可訪問) (anyRequest = 所有請求)<br>
	 * Restful API ( GET -> 訪問/查詢 ) ( POST -> 新增 ) ( PUT -> 更新) ( DELETE -> 刪除 )<br>
	 **/
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		// 下列-權限驗證

		// thirdparty && img 資料夾靜態資料可 直接 存取 (預設皆有 訪問權限 資料可[匿名]存取)
		http.authorizeHttpRequests().requestMatchers(HttpMethod.GET, "/thirdparty/**", "/img/**", "/login.basil", "/login.html").permitAll()
				// ----請求-index-(訪問)----
				.requestMatchers(HttpMethod.POST, "/ajax/index.basil").hasAuthority(actionRole("index.basil", ""))

				// ----請求-system_config-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_con).hasAuthority(actionRole(system_con, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_con + ".AR").hasAuthority(actionRole(system_con, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_con + ".AC").hasAuthority(actionRole(system_con, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_con + ".AU").hasAuthority(actionRole(system_con, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_con + ".AD").hasAuthority(actionRole(system_con, "AD"))// (移除)

				// ----請求-system_permission-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_per).hasAuthority(actionRole(system_per, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_per + ".AR").hasAuthority(actionRole(system_per, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_per + ".AC").hasAuthority(actionRole(system_per, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_per + ".AU").hasAuthority(actionRole(system_per, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_per + ".AD").hasAuthority(actionRole(system_per, "AD"))// (移除)

				// ----請求-sys_group-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_gro).hasAuthority(actionRole(system_gro, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_gro + ".AR").hasAuthority(actionRole(system_gro, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_gro + ".AC").hasAuthority(actionRole(system_gro, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_gro + ".AU").hasAuthority(actionRole(system_gro, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_gro + ".AD").hasAuthority(actionRole(system_gro, "AD"))// (移除)

				// ----請求-sys_user-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_use).hasAuthority(actionRole(system_use, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_use + ".AR").hasAuthority(actionRole(system_use, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_use + ".AC").hasAuthority(actionRole(system_use, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_use + ".AU").hasAuthority(actionRole(system_use, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_use + ".AD").hasAuthority(actionRole(system_use, "AD"))// (移除)

				// 以上-請求需要檢驗-全部請求
				.anyRequest().authenticated();

		// 登入機制
		http.formLogin()
				// 登入-預設登入頁面 預設帳密參數為(.usernameParameter(username).passwordParameter(password))
				.loginPage("/login.basil?status")
				// 登入-程序對象
				.loginProcessingUrl("/index.basil")
				// 登入-成功
				.successForwardUrl("/main.basil")
				// 登入-失敗
				.failureUrl("/login.basil?status=account or password incorrect!");

		// 登出機制
		http.logout()
				// 登出-預設登入頁面
				.logoutUrl("/logout.basil")
				// 登出-程序對象
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
				// 登出-後轉跳
				.logoutSuccessUrl("/login.basil?status=You are exit!")
				// 登出-移除Session
				.invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
				// 登出-移除Cookies
				.deleteCookies();
		// JWT 先攔截
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		// 關閉CSRF跨域
		http.csrf().disable();
		return http.build();
	}

	/**
	 * 權限-規則-群組歸類
	 **/
	private String actionRole(String cell_who, String action_do) {
		// sg_permission(000000000001)->12碼
		// S6[特殊6]/S5[特殊5]/S4[特殊4]/S3[特殊3]/S2[特殊2]/S1[特殊1]
		// AO[擁有]DD[完全移除]/AD[作廢]/AC[新增]/AU[更新]/AR[讀取]/AA[訪問]
		// 訪問
		String cell_role = cell_who.replace(".", "_").replace("/ajax/", "");
		String hasAuthority = cell_role + "_000000000001";// def:訪問
		// CRUD
		switch (action_do) {
		case "S6":
			hasAuthority = cell_role + "_100000000000"; // 特殊6(S6)
			break;
		case "S5":
			hasAuthority = cell_role + "_010000000000"; // 特殊5(S5)
			break;
		case "S4":
			hasAuthority = cell_role + "_001000000000"; // 特殊4(S4)
			break;
		case "S3":
			hasAuthority = cell_role + "_000100000000"; // 特殊3(S3)
			break;
		case "S2":
			hasAuthority = cell_role + "_000010000000"; // 特殊2(S2)
			break;
		case "S1":
			hasAuthority = cell_role + "_000001000000"; // 特殊1(S1)
			break;
		case "DD":
			hasAuthority = cell_role + "_000000100000"; // 完全移除(DD)
			break;
		case "AD":
			hasAuthority = cell_role + "_000000010000"; // 作廢(AD)
			break;
		case "AC":
			hasAuthority = cell_role + "_000000001000"; // 新增(AC)
			break;
		case "AU":
			hasAuthority = cell_role + "_000000000100"; // 修改(AU)
			break;
		case "AR":
			hasAuthority = cell_role + "_000000000010"; // 查詢(AR)
			break;
		case "AA":
			hasAuthority = cell_role + "_000000000001"; // 訪問(AA)
			break;
		default:
			break;
		}
		System.out.println(cell_who + " " + hasAuthority);
		return hasAuthority;
	}
}
