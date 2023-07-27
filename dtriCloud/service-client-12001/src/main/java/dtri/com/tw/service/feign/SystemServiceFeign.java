package dtri.com.tw.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import dtri.com.tw.shared.PackageBean;

/**
 * @FeignClient value=微服務器名稱 <br>
 *              path=位置<br>
 *              傳遞物件固定使用JSON傳遞
 * 
 **/
@Component
@FeignClient(value = "SERVICE-SYSTEM", path = "service-system")
public interface SystemServiceFeign {
	// ================================系統設定================================
	@RequestMapping(value = { "/systemConfig/getSearch" }, method = RequestMethod.POST)
	PackageBean getConfigSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/getReport" }, method = RequestMethod.POST)
	PackageBean getConfigReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setModify" }, method = RequestMethod.POST)
	PackageBean setConfigModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setAdd" }, method = RequestMethod.POST)
	PackageBean setConfigAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setInvalid" }, method = RequestMethod.POST)
	PackageBean setConfigInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setDetele" }, method = RequestMethod.POST)
	PackageBean setConfigDetele(@RequestBody String jsonPackageBean);

	// ================================系統語言================================
	@RequestMapping(value = { "/systemLanguageCell/getSearch" }, method = RequestMethod.POST)
	PackageBean getLanguageCellSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemLanguageCell/getReport" }, method = RequestMethod.POST)
	PackageBean getLanguageCellReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemLanguageCell/setModify" }, method = RequestMethod.POST)
	PackageBean setLanguageCellModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemLanguageCell/setAdd" }, method = RequestMethod.POST)
	PackageBean setLanguageCellAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemLanguageCell/setInvalid" }, method = RequestMethod.POST)
	PackageBean setLanguageCellInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemLanguageCell/setDetele" }, method = RequestMethod.POST)
	PackageBean setLanguageCellDetele(@RequestBody String jsonPackageBean);
	
	// ================================系統權限================================
	@RequestMapping(value = { "/systemPermission/getSearch" }, method = RequestMethod.POST)
	PackageBean getPermissionSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemPermission/getReport" }, method = RequestMethod.POST)
	PackageBean getPermissionReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemPermission/setModify" }, method = RequestMethod.POST)
	PackageBean setPermissionModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemPermission/setAdd" }, method = RequestMethod.POST)
	PackageBean setPermissionAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemPermission/setInvalid" }, method = RequestMethod.POST)
	PackageBean setPermissionInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemPermission/setDetele" }, method = RequestMethod.POST)
	PackageBean setPermissionDetele(@RequestBody String jsonPackageBean);

}
