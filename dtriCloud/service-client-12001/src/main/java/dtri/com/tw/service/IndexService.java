package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;

import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.SystemGroup;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.SystemUser;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class IndexService {
//	@Autowired
//	private JwtUtilities jwtUtilities;

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	/** 取得個人權限清單 */
	public PackageBean getMenu(PackageBean packageBean, SystemUser systemUser) throws Exception {

		// Step1.取得翻譯(一般)
		ArrayList<SystemLanguageCell> languages = languageDao.findAllBySystemLanguageCell(null, null, 1, null);
		Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
		languages.forEach(x -> {
			mapLanguages.put(x.getSltarget(), x);
			System.out.println(x.getSltarget() + " : " + x.getSllanguage());
		});
		// Step2.放入翻譯(一般)->過濾不需要的(spid=1 無作用 只是用來關聯)
		 Set<SystemGroup> newGroup = new HashSet<>();
		systemUser.getSystemgroups().forEach(group -> {
			String spcontrol = group.getSystemPermission().getSpcontrol();
			String spgroup = group.getSystemPermission().getSysheader() + "";
			String sgpermission = group.getSgpermission() + "";
			if ((mapLanguages.containsKey(spcontrol))) {
				group.getSystemPermission().setLanguage(mapLanguages.get(spcontrol).getSllanguage());
				group.getSystemPermission().setSppermission(sgpermission);
			}
			group.setSyscdate(null);
			System.out.println(spcontrol + " : " + spgroup + ":" + group.getSgname()+":"+sgpermission);
			
			if(group.getSystemPermission().getSpid().compareTo(1L)!=0) {
				newGroup.add(group);				
			}
		});
		systemUser.setSystemgroups(newGroup);
		// Step3.將使用者+群組功能權限=>打包
		String systemUserJson = packageService.beanToJson(systemUser);
		packageBean.setEntityJson(systemUserJson);
		return packageBean;
	}

	/** Token 令牌取得 */
//	public String getToken() throws Exception {
//		// Step1.取得使用者
//		List<String> rolesGroup = new ArrayList<String>();
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		CustomerUserDetails userDetails = new CustomerUserDetails();
//		if (!(authentication instanceof AnonymousAuthenticationToken)) {
//			userDetails = (CustomerUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//		}
//		// Step2.權限清單
//		userDetails.getSystemGroup().forEach(systemGroup -> {
//			String role = systemGroup.getSystemPermission().getSpcontrol().replaceAll("\\.", "_") //
//					+ "_"//
//					+ systemGroup.getSystemPermission().getSppermission();
//			rolesGroup.add(role);
//		});
//		// Step3.寫入(令牌)token
//		/*
//		 * authentication = authenticationManager .authenticate(new
//		 * UsernamePasswordAuthenticationToken(userDetails.getUsername(),
//		 * userDetails.getPassword()));
//		 * SecurityContextHolder.getContext().setAuthentication(authentication);
//		 */
//		// Step4.建立(令牌)token 憑證
//		String token = jwtUtilities.generateToken(userDetails.getUsername(), rolesGroup);
//		JsonObject innerObject = new JsonObject();
//		innerObject.addProperty("token", token);
//		return token;
//	}

	/** 測試 包裝/解包範例 */
	public PackageBean getTest(PackageBean packageBean) throws Exception {
		// 測試資料
		ArrayList<SystemUser> systemUsers = new ArrayList<SystemUser>();
		Set<SystemGroup> groups = new HashSet<SystemGroup>();
		SystemUser user = new SystemUser();
		SystemGroup group = new SystemGroup();
		group.setSgname("123");
		groups.add(group);
		user.setSystemgroups(groups);
		systemUsers.add(user);

		// 返回資料 => 轉換 Bean -> Json 打包
		String entityJson = packageService.beanToJson(systemUsers);
		packageBean.setEntityJson(entityJson);
		packageBean.setInfo("[000]測試內容");
		String packageJson = packageService.beanToJson(packageBean);

		// 接收資料 => 轉換 Json -> Bean 解包
		PackageBean packageBean2 = packageService.jsonToBean(packageJson, PackageBean.class);
		// 取出 第一層 資料
		String entityJson2 = packageBean2.getEntityJson();
		// 取出 第二層 資料
		ArrayList<SystemUser> user2 = packageService.jsonToBean(entityJson2, new TypeReference<ArrayList<SystemUser>>() {
		});
		// 異常錯誤
		if (user2 != null) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}
		return packageBean;
	}
}
