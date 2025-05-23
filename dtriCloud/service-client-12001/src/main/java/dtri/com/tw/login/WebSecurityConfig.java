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

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
//	@Autowired
//	JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	CutomerUserDetailsService customerUserDetailsService;

	// 手動補上建構子
	public WebSecurityConfig(CutomerUserDetailsService customerUserDetailsService) {
		this.customerUserDetailsService = customerUserDetailsService;
	}

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
	private static final String basic_bom = "/ajax/basic_bom_ingredients.basil";
	private static final String basic_com = "/ajax/basic_command_list.basil";
	private static final String basic_shi = "/ajax/basic_shipping_list.basil";
	private static final String basic_inc = "/ajax/basic_incoming_list.basil";
	private static final String basic_pdm = "/ajax/basic_product_model.basil";
	private static final String basic_ci = "/ajax/basic_change_items.basil";
	// BOM產品
	private static final String bom_bpm = "/ajax/bom_product_management.basil";
	private static final String bom_bpr = "/ajax/bom_product_rule.basil";
	private static final String bom_bps = "/ajax/bom_parameter_settings.basil";
	private static final String bom_bsh = "/ajax/bom_software_hardware.basil";
	private static final String bom_kee = "/ajax/bom_keeper.basil";
	private static final String bom_his = "/ajax/bom_history.basil";
	private static final String bom_sn = "/ajax/bom_shortage_notification.basil";

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
	private static final String manufacture_pro = "/ajax/manufacture_process_card.basil";
	private static final String manufacture_rul = "/ajax/manufacture_rule_number.basil";
	private static final String manufacture_ser = "/ajax/manufacture_serial_number.basil";
	// 生管
	private static final String schedule_sho = "/ajax/schedule_shortage_list.basil";
	private static final String schedule_out = "/ajax/schedule_outsourcer.basil";
	private static final String schedule_inf = "/ajax/schedule_infactory.basil";
	private static final String schedule_shn = "/ajax/schedule_shortage_notification.basil";
	private static final String schedule_sph = "/ajax/schedule_production_history.basil";
	private static final String schedule_spn = "/ajax/schedule_production_notes.basil";
	//物控
	private static final String material_rep = "/ajax/material_replacement.basil";

	// BIOS
	private static final String bios_not = "/ajax/bios_notification.basil";
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
				.requestMatchers(HttpMethod.GET, "/thirdparty/**", "/files/**", "/img/**", "/login.basil",
						"/login.html")
				.permitAll()
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

				// ----請求-basic_bom_ingredients-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_bom).hasAuthority(actionRole(basic_bom, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_bom + ".AR").hasAuthority(actionRole(basic_bom, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, basic_bom + ".ARR").hasAuthority(actionRole(basic_bom, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, basic_bom + ".AC").hasAuthority(actionRole(basic_bom, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, basic_bom + ".AU").hasAuthority(actionRole(basic_bom, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, basic_bom + ".AD").hasAuthority(actionRole(basic_bom, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, basic_bom + ".DD").hasAuthority(actionRole(basic_bom, "DD"))// (標記移除)

				// ----請求-basic_change_items-(訪問) ----
				.requestMatchers(HttpMethod.POST, basic_ci).hasAuthority(actionRole(basic_ci, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, basic_ci + ".AR").hasAuthority(actionRole(basic_ci, "AR"))// (查詢)

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

				// 產品功能
				// ----請求-bom_parameter_settings-(訪問) ----
				.requestMatchers(HttpMethod.POST, bom_bps).hasAuthority(actionRole(bom_bps, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bom_bps + ".AR").hasAuthority(actionRole(bom_bps, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_bps + ".ART").hasAuthority(actionRole(bom_bps, "AR"))// (測試查詢)
				.requestMatchers(HttpMethod.POST, bom_bps + ".ARR").hasAuthority(actionRole(bom_bps, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bom_bps + ".AC").hasAuthority(actionRole(bom_bps, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_bps + ".AU").hasAuthority(actionRole(bom_bps, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_bps + ".AD").hasAuthority(actionRole(bom_bps, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_bps + ".DD").hasAuthority(actionRole(bom_bps, "DD"))// (標記移除)
				// ----請求-bom_product_management-(訪問) ----
				.requestMatchers(HttpMethod.POST, bom_bpm).hasAuthority(actionRole(bom_bpm, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bom_bpm + ".AR").hasAuthority(actionRole(bom_bpm, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_bpm + ".WM").hasAuthority(actionRole(bom_bpm, "AR"))// (查詢(物料清單))
				.requestMatchers(HttpMethod.POST, bom_bpm + ".ART").hasAuthority(actionRole(bom_bpm, "AR"))// (測試查詢)
				.requestMatchers(HttpMethod.POST, bom_bpm + ".ARR").hasAuthority(actionRole(bom_bpm, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bom_bpm + ".AC").hasAuthority(actionRole(bom_bpm, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_bpm + ".AU").hasAuthority(actionRole(bom_bpm, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_bpm + ".AD").hasAuthority(actionRole(bom_bpm, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_bpm + ".DD").hasAuthority(actionRole(bom_bpm, "DD"))// (標記移除)
				// ----請求-bom_product_rule-(訪問)->特例府屬bom_product_management ----
				.requestMatchers(HttpMethod.POST, bom_bpr + ".AR").hasAuthority(actionRole(bom_bpm, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_bpr + ".AC").hasAuthority(actionRole(bom_bpm, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_bpr + ".AU").hasAuthority(actionRole(bom_bpm, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_bpr + ".AD").hasAuthority(actionRole(bom_bpm, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_bpr + ".DD").hasAuthority(actionRole(bom_bpm, "DD"))// (標記移除)

				// ----請求-bom_software_hardware-(訪問) ----
				.requestMatchers(HttpMethod.POST, bom_bsh).hasAuthority(actionRole(bom_bsh, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bom_bsh + ".AR").hasAuthority(actionRole(bom_bsh, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_bsh + ".ARR").hasAuthority(actionRole(bom_bsh, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bom_bsh + ".AC").hasAuthority(actionRole(bom_bsh, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_bsh + ".AU").hasAuthority(actionRole(bom_bsh, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_bsh + ".AD").hasAuthority(actionRole(bom_bsh, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_bsh + ".DD").hasAuthority(actionRole(bom_bsh, "DD"))// (標記移除)

				// ----請求-bom_keeper-(訪問) ----
				.requestMatchers(HttpMethod.POST, bom_kee).hasAuthority(actionRole(bom_kee, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bom_kee + ".AR").hasAuthority(actionRole(bom_kee, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_kee + ".ARR").hasAuthority(actionRole(bom_kee, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bom_kee + ".AC").hasAuthority(actionRole(bom_kee, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_kee + ".AU").hasAuthority(actionRole(bom_kee, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_kee + ".AD").hasAuthority(actionRole(bom_kee, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_kee + ".DD").hasAuthority(actionRole(bom_kee, "DD"))// (標記移除)
				// ----請求-bom_history-(訪問) ----
				.requestMatchers(HttpMethod.POST, bom_his).hasAuthority(actionRole(bom_his, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bom_his + ".AR").hasAuthority(actionRole(bom_his, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_his + ".ARR").hasAuthority(actionRole(bom_his, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bom_his + ".AC").hasAuthority(actionRole(bom_his, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_his + ".AU").hasAuthority(actionRole(bom_his, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_his + ".AD").hasAuthority(actionRole(bom_his, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_his + ".DD").hasAuthority(actionRole(bom_his, "DD"))// (標記移除)
				// ----bom_shortage_notification-(訪問) ----
				.requestMatchers(HttpMethod.POST, bom_sn).hasAuthority(actionRole(bom_sn, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bom_sn + ".AR").hasAuthority(actionRole(bom_sn, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bom_sn + ".ARR").hasAuthority(actionRole(bom_sn, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bom_sn + ".AC").hasAuthority(actionRole(bom_sn, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bom_sn + ".AU").hasAuthority(actionRole(bom_sn, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bom_sn + ".AD").hasAuthority(actionRole(bom_sn, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bom_sn + ".DD").hasAuthority(actionRole(bom_sn, "DD"))// (標記移除)

				// 倉庫功能-客製化
				// ----請求-warehouse_assignment-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_ass).hasAuthority(actionRole(warehouse_ass, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_ass + ".AR").hasAuthority(actionRole(warehouse_ass, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, warehouse_ass + ".ARE").hasAuthority(actionRole(warehouse_ass, "AR"))// (立即同步單據)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".SS1").hasAuthority(actionRole(warehouse_ass, "S1"))// (修改S1->打印標記)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S1").hasAuthority(actionRole(warehouse_ass, "S1"))// (修改S1)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S2").hasAuthority(actionRole(warehouse_ass, "S2"))// (修改S2)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S3").hasAuthority(actionRole(warehouse_ass, "S3"))// (修改S3)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S4").hasAuthority(actionRole(warehouse_ass, "S4"))// (修改S4)
				.requestMatchers(HttpMethod.PUT, warehouse_ass + ".S5").hasAuthority(actionRole(warehouse_ass, "S5"))// (修改S5)

				// -客製化
				// ----請求-warehouse_action-(訪問) ----
				.requestMatchers(HttpMethod.POST, warehouse_act).hasAuthority(actionRole(warehouse_act, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, warehouse_act + ".AR").hasAuthority(actionRole(warehouse_act, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, warehouse_act + ".S1").hasAuthority(actionRole(warehouse_act, "S1"))// (修改S1)
				.requestMatchers(HttpMethod.PUT, warehouse_act + ".SS1").hasAuthority(actionRole(warehouse_act, "S1"))// (修改S1)

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
				// ----請求-schedule_out-(訪問) ----
				.requestMatchers(HttpMethod.POST, schedule_out).hasAuthority(actionRole(schedule_out, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, schedule_out + ".AR").hasAuthority(actionRole(schedule_out, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, schedule_out + ".S1").hasAuthority(actionRole(schedule_out, "S1"))// (修改S1)
				.requestMatchers(HttpMethod.PUT, schedule_out + ".S2").hasAuthority(actionRole(schedule_out, "S2"))// (修改S2)
				.requestMatchers(HttpMethod.PUT, schedule_out + ".S3").hasAuthority(actionRole(schedule_out, "S3"))// (修改S3)
				.requestMatchers(HttpMethod.PUT, schedule_out + ".S4").hasAuthority(actionRole(schedule_out, "S4"))// (修改S4)
				.requestMatchers(HttpMethod.PUT, schedule_out + ".S4").hasAuthority(actionRole(schedule_out, "S5"))// (修改S5)
				.requestMatchers(HttpMethod.GET, "/websocket/schedule_outsourcer_client/echo").permitAll()// 前端-請求(Websocket)
				.requestMatchers(HttpMethod.POST, "/websocket/schedule_outsourcer_service").permitAll()// 後端-同步使用(Websocket)
				// ----請求-schedule_inf-(訪問) ----
				.requestMatchers(HttpMethod.POST, schedule_inf).hasAuthority(actionRole(schedule_inf, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, schedule_inf + ".AR").hasAuthority(actionRole(schedule_inf, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, schedule_inf + ".S1").hasAuthority(actionRole(schedule_inf, "S1"))// (修改S1)
				.requestMatchers(HttpMethod.PUT, schedule_inf + ".S2").hasAuthority(actionRole(schedule_inf, "S2"))// (修改S2)
				.requestMatchers(HttpMethod.PUT, schedule_inf + ".S3").hasAuthority(actionRole(schedule_inf, "S3"))// (修改S3)
				.requestMatchers(HttpMethod.PUT, schedule_inf + ".S4").hasAuthority(actionRole(schedule_inf, "S4"))// (修改S4)
				.requestMatchers(HttpMethod.PUT, schedule_inf + ".S4").hasAuthority(actionRole(schedule_inf, "S5"))// (修改S5)
				.requestMatchers(HttpMethod.GET, "/websocket/schedule_infactory_client/echo").permitAll()// 前端-請求(Websocket)
				.requestMatchers(HttpMethod.POST, "/websocket/schedule_infactory_service").permitAll()// 後端-同步使用(Websocket)
				.requestMatchers(HttpMethod.POST, "/websocket/schedule_infactory_dft_service").permitAll()// 後端-同步使用(取得標記)
				// ----請求-schedule_shortage_notification-(訪問) ----
				.requestMatchers(HttpMethod.POST, schedule_shn).hasAuthority(actionRole(schedule_shn, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, schedule_shn + ".AR").hasAuthority(actionRole(schedule_shn, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, schedule_shn + ".ARR").hasAuthority(actionRole(schedule_shn, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, schedule_shn + ".AC").hasAuthority(actionRole(schedule_shn, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, schedule_shn + ".AU").hasAuthority(actionRole(schedule_shn, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, schedule_shn + ".AD").hasAuthority(actionRole(schedule_shn, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, schedule_shn + ".DD").hasAuthority(actionRole(schedule_shn, "DD"))// (標記移除)
				// ----請求-schedule_production_history-(訪問) ----
				.requestMatchers(HttpMethod.POST, schedule_sph).hasAuthority(actionRole(schedule_sph, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, schedule_sph + ".AR").hasAuthority(actionRole(schedule_sph, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, schedule_sph + ".ARR").hasAuthority(actionRole(schedule_sph, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, schedule_sph + ".AC").hasAuthority(actionRole(schedule_sph, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, schedule_sph + ".AU").hasAuthority(actionRole(schedule_sph, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, schedule_sph + ".AD").hasAuthority(actionRole(schedule_sph, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, schedule_sph + ".DD").hasAuthority(actionRole(schedule_sph, "DD"))// (標記移除)
				// ----請求-schedule_production_notes-(訪問) ----
				.requestMatchers(HttpMethod.POST, schedule_spn).hasAuthority(actionRole(schedule_spn, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, schedule_spn + ".AR").hasAuthority(actionRole(schedule_spn, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, schedule_spn + ".ARO").hasAuthority(actionRole(schedule_spn, "AR"))// (在途單據查詢)
				.requestMatchers(HttpMethod.POST, schedule_spn + ".AC").hasAuthority(actionRole(schedule_spn, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, schedule_spn + ".AU").hasAuthority(actionRole(schedule_spn, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, schedule_spn + ".DD").hasAuthority(actionRole(schedule_spn, "DD"))// (標記移除)

				//物控
				// ----請求-schedule_production_notes-(訪問) ----
				.requestMatchers(HttpMethod.POST, material_rep).hasAuthority(actionRole(material_rep, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, material_rep + ".AR").hasAuthority(actionRole(material_rep, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, material_rep + ".ARR").hasAuthority(actionRole(material_rep, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, material_rep + ".AC").hasAuthority(actionRole(material_rep, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, material_rep + ".AU").hasAuthority(actionRole(material_rep, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, material_rep + ".AD").hasAuthority(actionRole(material_rep, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, material_rep + ".DD").hasAuthority(actionRole(material_rep, "DD"))// (標記移除)
				
				// -客製化
				// ----請求-manufacture_action-(訪問) ----
				.requestMatchers(HttpMethod.POST, manufacture_act).hasAuthority(actionRole(manufacture_act, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, manufacture_act + ".AR")
				.hasAuthority(actionRole(manufacture_act, "AR"))// (查詢)
				.requestMatchers(HttpMethod.PUT, manufacture_act + ".S1")
				.hasAuthority(actionRole(manufacture_act, "S1"))// (修改S1)
				// ----請求-manufacture_process_card-(訪問) ----
				.requestMatchers(HttpMethod.POST, manufacture_pro).hasAuthority(actionRole(manufacture_pro, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, manufacture_pro + ".AR").hasAuthority(actionRole(manufacture_pro, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, manufacture_pro + ".ARR").hasAuthority(actionRole(manufacture_pro, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, manufacture_pro + ".AC").hasAuthority(actionRole(manufacture_pro, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, manufacture_pro + ".AU").hasAuthority(actionRole(manufacture_pro, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, manufacture_pro + ".AD").hasAuthority(actionRole(manufacture_pro, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, manufacture_pro + ".DD").hasAuthority(actionRole(manufacture_pro, "DD"))// (標記移除)
				// ----請求-manufacture_rule_number-(訪問) ----
				.requestMatchers(HttpMethod.POST, manufacture_rul).hasAuthority(actionRole(manufacture_rul, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, manufacture_rul + ".AR").hasAuthority(actionRole(manufacture_rul, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, manufacture_rul + ".ARR").hasAuthority(actionRole(manufacture_rul, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, manufacture_rul + ".AC").hasAuthority(actionRole(manufacture_rul, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, manufacture_rul + ".AU").hasAuthority(actionRole(manufacture_rul, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, manufacture_rul + ".AD").hasAuthority(actionRole(manufacture_rul, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, manufacture_rul + ".DD").hasAuthority(actionRole(manufacture_rul, "DD"))// (標記移除)
				// ----請求-manufacture_serial_number-(訪問) ----
				.requestMatchers(HttpMethod.POST, manufacture_ser).hasAuthority(actionRole(manufacture_ser, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, manufacture_ser + ".AR").hasAuthority(actionRole(manufacture_ser, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, manufacture_ser + ".ARR").hasAuthority(actionRole(manufacture_ser, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, manufacture_ser + ".AC").hasAuthority(actionRole(manufacture_ser, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, manufacture_ser + ".AU").hasAuthority(actionRole(manufacture_ser, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, manufacture_ser + ".AD").hasAuthority(actionRole(manufacture_ser, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, manufacture_ser + ".DD").hasAuthority(actionRole(manufacture_ser, "DD"))// (標記移除)
				

				// BIOS版本控管
				// ----請求-bios_notification-(訪問) ----
				.requestMatchers(HttpMethod.POST, bios_not).hasAuthority(actionRole(bios_not, ""))// (轉跳)
				.requestMatchers(HttpMethod.POST, bios_not + ".AR").hasAuthority(actionRole(bios_not, "AR"))// (查詢)
				.requestMatchers(HttpMethod.POST, bios_not + ".ARR").hasAuthority(actionRole(bios_not, "AR"))// (報告查詢)
				.requestMatchers(HttpMethod.POST, bios_not + ".AC").hasAuthority(actionRole(bios_not, "AC"))// (新增)
				.requestMatchers(HttpMethod.PUT, bios_not + ".AU").hasAuthority(actionRole(bios_not, "AU"))// (修改)
				.requestMatchers(HttpMethod.DELETE, bios_not + ".AD").hasAuthority(actionRole(bios_not, "AD"))// (移除)
				.requestMatchers(HttpMethod.DELETE, bios_not + ".DD").hasAuthority(actionRole(bios_not, "DD"))// (標記移除)

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
