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

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
//	@Autowired
//	JwtAuthenticationFilter jwtAuthenticationFilter;
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
	private static final String system_lan = "/ajax/system_language_cell.basil";
	private static final String own_use = "/ajax/own_user.basil";
	// ERP同步
	private static final String basic_com = "/ajax/basic_command_list.basil";
	private static final String basic_shi = "/ajax/basic_shipping_list.basil";
	private static final String basic_inc = "/ajax/basic_incoming_list.basil";
	private static final String basic_pdm = "/ajax/basic_product_model.basil";
	// 信件
	private static final String basic_not = "/ajax/basic_notification_mail.basil";
	// 倉儲
	private static final String warehouse_his = "/ajax/warehouse_history.basil";
	private static final String warehouse_con = "/ajax/warehouse_config.basil";
	private static final String warehouse_typ = "/ajax/warehouse_type_filter.basil";
	private static final String warehouse_mat = "/ajax/warehouse_material.basil";
	private static final String warehouse_are = "/ajax/warehouse_area.basil";
	private static final String warehouse_kee = "/ajax/warehouse_keeper.basil";
	// 倉儲-客製化(發配單-執行單-同步物料)
	private static final String warehouse_ass = "/ajax/warehouse_assignment.basil";
	private static final String warehouse_act = "/ajax/warehouse_action.basil";
	private static final String warehouse_syn = "/ajax/warehouse_synchronize.basil";
	// 製造
	private static final String manufacture_act = "/ajax/manufacture_action.basil";
	// 生管
	private static final String schedule_sho = "/ajax/schedule_shortage_list.basil";
	// BIOS
	private static final String bios_pri = "/ajax/bios_principal.basil";
	private static final String bios_ver = "/ajax/bios_version.basil";
	private static final String bios_lif = "/ajax/bios_life_cycle.basil";
	private static final String bios_cus = "/ajax/bios_customer_tag.basil";

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
		http.authorizeHttpRequests()
				.requestMatchers(HttpMethod.GET, "/thirdparty/**", "/img/**", "/login.basil", "/login.html").permitAll()
				// ----請求-index-(訪問)----
				.requestMatchers(HttpMethod.POST, "/ajax/index.basil").hasAuthority(actionRole("index.basil", ""))

				// 系統功能
				// ----請求-system_config-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_con).hasAuthority(actionRole(system_con, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_con + ".AR").hasAuthority(actionRole(system_con, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_con + ".ARR").hasAuthority(actionRole(system_con, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, system_con + ".AC").hasAuthority(actionRole(system_con, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_con + ".AU").hasAuthority(actionRole(system_con, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_con + ".AD").hasAuthority(actionRole(system_con, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, system_con + ".DD").hasAuthority(actionRole(system_con, "DD"))// (標記移除)

				// ----請求-system_langage-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_lan).hasAuthority(actionRole(system_lan, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_lan + ".AR").hasAuthority(actionRole(system_lan, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_lan + ".ARR").hasAuthority(actionRole(system_lan, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, system_lan + ".AC").hasAuthority(actionRole(system_lan, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_lan + ".AU").hasAuthority(actionRole(system_lan, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_lan + ".AD").hasAuthority(actionRole(system_lan, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, system_lan + ".DD").hasAuthority(actionRole(system_lan, "DD"))// (標記移除)

				// ----請求-system_permission-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_per).hasAuthority(actionRole(system_per, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_per + ".AR").hasAuthority(actionRole(system_per, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_per + ".AC").hasAuthority(actionRole(system_per, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_per + ".AU").hasAuthority(actionRole(system_per, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_per + ".AD").hasAuthority(actionRole(system_per, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, system_per + ".DD").hasAuthority(actionRole(system_per, "DD"))// (標記移除)

				// ----請求-sys_group-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_gro).hasAuthority(actionRole(system_gro, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_gro + ".AR").hasAuthority(actionRole(system_gro, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_gro + ".AC").hasAuthority(actionRole(system_gro, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_gro + ".AU").hasAuthority(actionRole(system_gro, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_gro + ".AD").hasAuthority(actionRole(system_gro, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, system_gro + ".DD").hasAuthority(actionRole(system_gro, "DD"))// (標記移除)

				// ----請求-sys_user-(訪問) ----
				.requestMatchers(HttpMethod.POST, system_use).hasAuthority(actionRole(system_use, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, system_use + ".AR").hasAuthority(actionRole(system_use, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, system_use + ".AC").hasAuthority(actionRole(system_use, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, system_use + ".AU").hasAuthority(actionRole(system_use, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, system_use + ".AD").hasAuthority(actionRole(system_use, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, system_use + ".DD").hasAuthority(actionRole(system_use, "DD"))// (標記移除)

				// ----請求-sys_user-(訪問) ----
				.requestMatchers(HttpMethod.POST, own_use).hasAuthority(actionRole(own_use, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, own_use + ".AR").hasAuthority(actionRole(own_use, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, own_use + ".AU").hasAuthority(actionRole(own_use, "AU"))// (修改)

				// 基本單據功能

				// ----請求-basic_notification_mail-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_not).hasAuthority(actionRole(basic_not, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_not + ".AR").hasAuthority(actionRole(basic_not, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, basic_not + ".ARR").hasAuthority(actionRole(basic_not, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, basic_not + ".AC").hasAuthority(actionRole(basic_not, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, basic_not + ".AU").hasAuthority(actionRole(basic_not, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, basic_not + ".AD").hasAuthority(actionRole(basic_not, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, basic_not + ".DD").hasAuthority(actionRole(basic_not, "DD"))// (標記移除)

				// ----請求-basic_command_list-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_com).hasAuthority(actionRole(basic_com, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_com + ".AR").hasAuthority(actionRole(basic_com, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, basic_com + ".ARR").hasAuthority(actionRole(basic_com, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, basic_com + ".AC").hasAuthority(actionRole(basic_com, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, basic_com + ".AU").hasAuthority(actionRole(basic_com, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, basic_com + ".AD").hasAuthority(actionRole(basic_com, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, basic_com + ".DD").hasAuthority(actionRole(basic_com, "DD"))// (標記移除)

				// ----請求-basic_shipping_list-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_shi).hasAuthority(actionRole(basic_shi, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_shi + ".AR").hasAuthority(actionRole(basic_shi, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, basic_shi + ".ARR").hasAuthority(actionRole(basic_shi, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, basic_shi + ".AC").hasAuthority(actionRole(basic_shi, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, basic_shi + ".AU").hasAuthority(actionRole(basic_shi, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, basic_shi + ".AD").hasAuthority(actionRole(basic_shi, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, basic_shi + ".DD").hasAuthority(actionRole(basic_shi, "DD"))// (標記移除)

				// ----請求-basic_incoming_list-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_inc).hasAuthority(actionRole(basic_inc, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_inc + ".AR").hasAuthority(actionRole(basic_inc, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, basic_inc + ".ARR").hasAuthority(actionRole(basic_inc, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, basic_inc + ".AC").hasAuthority(actionRole(basic_inc, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, basic_inc + ".AU").hasAuthority(actionRole(basic_inc, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, basic_inc + ".AD").hasAuthority(actionRole(basic_inc, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, basic_inc + ".DD").hasAuthority(actionRole(basic_inc, "DD"))// (標記移除)

				// ----請求-basic_product_model-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_pdm).hasAuthority(actionRole(basic_pdm, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_pdm + ".AR").hasAuthority(actionRole(basic_pdm, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, basic_pdm + ".ARR").hasAuthority(actionRole(basic_pdm, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, basic_pdm + ".AC").hasAuthority(actionRole(basic_pdm, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, basic_pdm + ".AU").hasAuthority(actionRole(basic_pdm, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, basic_pdm + ".AD").hasAuthority(actionRole(basic_pdm, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, basic_pdm + ".DD").hasAuthority(actionRole(basic_pdm, "DD"))// (標記移除)

				// 倉庫功能-客製化
				// ----請求-warehouse_assignment-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_ass).hasAuthority(actionRole(warehouse_ass, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_ass + ".AR").hasAuthority(actionRole(warehouse_ass, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_ass + ".ARE").hasAuthority(actionRole(warehouse_ass, "AR"))// (立即同步單據)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S1").hasAuthority(actionRole(warehouse_ass, "S1"))// (修改S1)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".SS1").hasAuthority(actionRole(warehouse_ass, "S1"))// (修改S1->打印標記)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S2").hasAuthority(actionRole(warehouse_ass, "S2"))// (修改S2)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S3").hasAuthority(actionRole(warehouse_ass, "S3"))// (修改S3)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S4").hasAuthority(actionRole(warehouse_ass, "S4"))// (修改S4)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S5").hasAuthority(actionRole(warehouse_ass, "S5"))// (修改S5)

				// -客製化
				// ----請求-warehouse_action-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_act).hasAuthority(actionRole(warehouse_act, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_act + ".AR").hasAuthority(actionRole(warehouse_act, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, warehouse_act + ".S1").hasAuthority(actionRole(warehouse_act, "S1"))// (修改S1)

				// -客製化
				// ----請求-warehouse_synchronize-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_syn).hasAuthority(actionRole(warehouse_syn, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_syn + ".AR").hasAuthority(actionRole(warehouse_syn, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, warehouse_syn + ".S1").hasAuthority(actionRole(warehouse_syn, "S1"))// (修改S1)
				.requestMatchers(HttpMethod.PUT, warehouse_syn + ".S2").hasAuthority(actionRole(warehouse_syn, "S2"))// (修改S2)
				.requestMatchers(HttpMethod.PUT, warehouse_syn + ".S3").hasAuthority(actionRole(warehouse_syn, "S3"))// (修改S3)
				.requestMatchers(HttpMethod.PUT, warehouse_syn + ".S4").hasAuthority(actionRole(warehouse_syn, "S4"))// (修改S4)

				// 倉庫功能-基本
				// ----請求-warehouse_history-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_his).hasAuthority(actionRole(warehouse_his, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_his + ".AR").hasAuthority(actionRole(warehouse_his, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_his + ".ARR").hasAuthority(actionRole(warehouse_his, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, warehouse_his + ".AC").hasAuthority(actionRole(warehouse_his, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, warehouse_his + ".AU").hasAuthority(actionRole(warehouse_his, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, warehouse_his + ".AD").hasAuthority(actionRole(warehouse_his, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, warehouse_his + ".DD").hasAuthority(actionRole(warehouse_his, "DD"))// (標記移除)

				// ----請求-warehouse_config-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_con).hasAuthority(actionRole(warehouse_con, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_con + ".AR").hasAuthority(actionRole(warehouse_con, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_con + ".ARR").hasAuthority(actionRole(warehouse_con, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, warehouse_con + ".AC").hasAuthority(actionRole(warehouse_con, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, warehouse_con + ".AU").hasAuthority(actionRole(warehouse_con, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, warehouse_con + ".AD").hasAuthority(actionRole(warehouse_con, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, warehouse_con + ".DD").hasAuthority(actionRole(warehouse_con, "DD"))// (標記移除)

				// ----請求-warehouse_type_filter-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_typ).hasAuthority(actionRole(warehouse_typ, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_typ + ".AR").hasAuthority(actionRole(warehouse_typ, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_typ + ".ARR").hasAuthority(actionRole(warehouse_typ, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, warehouse_typ + ".AC").hasAuthority(actionRole(warehouse_typ, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, warehouse_typ + ".AU").hasAuthority(actionRole(warehouse_typ, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, warehouse_typ + ".AD").hasAuthority(actionRole(warehouse_typ, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, warehouse_typ + ".DD").hasAuthority(actionRole(warehouse_typ, "DD"))// (標記移除)

				// ----請求-warehouse_material-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_mat).hasAuthority(actionRole(warehouse_mat, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_mat + ".AR").hasAuthority(actionRole(warehouse_mat, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_mat + ".ARR").hasAuthority(actionRole(warehouse_mat, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, warehouse_mat + ".AC").hasAuthority(actionRole(warehouse_mat, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, warehouse_mat + ".AU").hasAuthority(actionRole(warehouse_mat, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, warehouse_mat + ".AD").hasAuthority(actionRole(warehouse_mat, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, warehouse_mat + ".DD").hasAuthority(actionRole(warehouse_mat, "DD"))// (標記移除)

				// ----請求-warehouse_area-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_are).hasAuthority(actionRole(warehouse_are, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_are + ".AR").hasAuthority(actionRole(warehouse_are, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_are + ".ARR").hasAuthority(actionRole(warehouse_are, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, warehouse_are + ".AC").hasAuthority(actionRole(warehouse_are, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, warehouse_are + ".AU").hasAuthority(actionRole(warehouse_are, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, warehouse_are + ".AD").hasAuthority(actionRole(warehouse_are, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, warehouse_are + ".DD").hasAuthority(actionRole(warehouse_are, "DD"))// (標記移除)

				// ----請求-warehouse_keeper-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_kee).hasAuthority(actionRole(warehouse_kee, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_kee + ".AR").hasAuthority(actionRole(warehouse_kee, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_kee + ".ARR").hasAuthority(actionRole(warehouse_kee, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, warehouse_kee + ".AC").hasAuthority(actionRole(warehouse_kee, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, warehouse_kee + ".AU").hasAuthority(actionRole(warehouse_kee, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, warehouse_kee + ".AD").hasAuthority(actionRole(warehouse_kee, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, warehouse_kee + ".DD").hasAuthority(actionRole(warehouse_kee, "DD"))// (標記移除)

				// 生管功能
				// ----請求-schedule_shortage_list-(訪問) ----
				.requestMatchers(HttpMethod.POST, schedule_sho).hasAuthority(actionRole(schedule_sho, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, schedule_sho + ".AR").hasAuthority(actionRole(schedule_sho, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, schedule_sho + ".ARR").hasAuthority(actionRole(schedule_sho, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, schedule_sho + ".AC").hasAuthority(actionRole(schedule_sho, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, schedule_sho + ".AU").hasAuthority(actionRole(schedule_sho, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, schedule_sho + ".AD").hasAuthority(actionRole(schedule_sho, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, schedule_sho + ".DD").hasAuthority(actionRole(schedule_sho, "DD"))// (標記移除)

				// -客製化
				// ----請求-manufacture_action-(訪問) ----
				.requestMatchers(HttpMethod.POST, manufacture_act).hasAuthority(actionRole(manufacture_act, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, manufacture_act + ".AR")
				.hasAuthority(actionRole(manufacture_act, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, manufacture_act + ".S1")
				.hasAuthority(actionRole(manufacture_act, "S1"))// (修改S1)

				// BIOS版本控管
				// ----請求-bios_principal-(訪問) ----
				.requestMatchers(HttpMethod.POST, bios_pri).hasAuthority(actionRole(bios_pri, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bios_pri + ".AR").hasAuthority(actionRole(bios_pri, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bios_pri + ".ARR").hasAuthority(actionRole(bios_pri, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bios_pri + ".AC").hasAuthority(actionRole(bios_pri, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bios_pri + ".AU").hasAuthority(actionRole(bios_pri, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bios_pri + ".AD").hasAuthority(actionRole(bios_pri, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bios_pri + ".DD").hasAuthority(actionRole(bios_pri, "DD"))// (標記移除)

				// ----請求-bios_version-(訪問) ----
				.requestMatchers(HttpMethod.POST, bios_ver).hasAuthority(actionRole(bios_ver, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bios_ver + ".AR").hasAuthority(actionRole(bios_ver, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bios_ver + ".ARR").hasAuthority(actionRole(bios_ver, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bios_ver + ".AC").hasAuthority(actionRole(bios_ver, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bios_ver + ".AU").hasAuthority(actionRole(bios_ver, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bios_ver + ".AD").hasAuthority(actionRole(bios_ver, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bios_ver + ".DD").hasAuthority(actionRole(bios_ver, "DD"))// (標記移除)
				// ----請求-bios_version-(訪問) ----
				.requestMatchers(HttpMethod.POST, bios_lif).hasAuthority(actionRole(bios_lif, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bios_lif + ".AR").hasAuthority(actionRole(bios_lif, "AR"))// (查詢)

				// ----請求-bios_customer_tag-(訪問) ----
				.requestMatchers(HttpMethod.POST, bios_cus).hasAuthority(actionRole(bios_cus, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bios_cus + ".AR").hasAuthority(actionRole(bios_cus, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bios_cus + ".ARR").hasAuthority(actionRole(bios_cus, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bios_cus + ".AC").hasAuthority(actionRole(bios_cus, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bios_cus + ".AU").hasAuthority(actionRole(bios_cus, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bios_cus + ".AD").hasAuthority(actionRole(bios_cus, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bios_cus + ".DD").hasAuthority(actionRole(bios_cus, "DD"))// (標記移除)

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
		// http.addFilterBefore(jwtAuthenticationFilter,
		// UsernamePasswordAuthenticationFilter.class);
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
