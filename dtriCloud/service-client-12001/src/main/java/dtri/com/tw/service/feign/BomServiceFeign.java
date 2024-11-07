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
@FeignClient(value = "SERVICE-BOM", path = "service-bom")
public interface BomServiceFeign {
	// ================================產品:軟硬體版本================================
	@RequestMapping(value = { "/bomSoftwareHardware/getSearch" }, method = RequestMethod.POST)
	PackageBean getSoftwareHardwareSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomSoftwareHardware/getReport" }, method = RequestMethod.POST)
	PackageBean getSoftwareHardwareReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomSoftwareHardware/setModify" }, method = RequestMethod.POST)
	PackageBean setSoftwareHardwareModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomSoftwareHardware/setAdd" }, method = RequestMethod.POST)
	PackageBean setSoftwareHardwareAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomSoftwareHardware/setInvalid" }, method = RequestMethod.POST)
	PackageBean setSoftwareHardwareInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomSoftwareHardware/setDetele" }, method = RequestMethod.POST)
	PackageBean setSoftwareHardwareDetele(@RequestBody String jsonPackageBean);

	// ================================產品:產品BOM設定參數配置================================
	@RequestMapping(value = { "/bomParameterSettings/getSearch" }, method = RequestMethod.POST)
	PackageBean getParameterSettingsSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomParameterSettings/getReport" }, method = RequestMethod.POST)
	PackageBean getParameterSettingsReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomParameterSettings/setModify" }, method = RequestMethod.POST)
	PackageBean setParameterSettingsModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomParameterSettings/setAdd" }, method = RequestMethod.POST)
	PackageBean setParameterSettingsAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomParameterSettings/setInvalid" }, method = RequestMethod.POST)
	PackageBean setParameterSettingsInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomParameterSettings/setDetele" }, method = RequestMethod.POST)
	PackageBean setParameterSettingsDetele(@RequestBody String jsonPackageBean);

	// ================================產品:物料規格項目================================
	@RequestMapping(value = { "/bomItemSpecifications/getSearch" }, method = RequestMethod.POST)
	PackageBean getItemSpecificationsSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomItemSpecifications/getSearchTest" }, method = RequestMethod.POST)
	PackageBean getItemSpecificationsSearchTest(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomItemSpecifications/getReport" }, method = RequestMethod.POST)
	PackageBean getItemSpecificationsReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomItemSpecifications/setModify" }, method = RequestMethod.POST)
	PackageBean setItemSpecificationsModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomItemSpecifications/setAdd" }, method = RequestMethod.POST)
	PackageBean setItemSpecificationsAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomItemSpecifications/setInvalid" }, method = RequestMethod.POST)
	PackageBean setItemSpecificationsInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomItemSpecifications/setDetele" }, method = RequestMethod.POST)
	PackageBean setItemSpecificationsDetele(@RequestBody String jsonPackageBean);

	// ================================產品:BOM產品管理================================
	@RequestMapping(value = { "/bomProductManagement/getSearch" }, method = RequestMethod.POST)
	PackageBean getProductManagementSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductManagement/getSearchWM" }, method = RequestMethod.POST)
	PackageBean getProductManagementgetSearchWM(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductManagement/getReport" }, method = RequestMethod.POST)
	PackageBean getProductManagementReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductManagement/setModify" }, method = RequestMethod.POST)
	PackageBean setProductManagementModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductManagement/setAdd" }, method = RequestMethod.POST)
	PackageBean setProductManagementAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductManagement/setInvalid" }, method = RequestMethod.POST)
	PackageBean setProductManagementInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductManagement/setDetele" }, method = RequestMethod.POST)
	PackageBean setProductManagementDetele(@RequestBody String jsonPackageBean);

	// ================================產品:BOM產品規則================================
	@RequestMapping(value = { "/bomProductRule/getSearch" }, method = RequestMethod.POST)
	PackageBean getProductRuleSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductRule/setModify" }, method = RequestMethod.POST)
	PackageBean setProductRuleModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductRule/setAdd" }, method = RequestMethod.POST)
	PackageBean setProductRuleAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductRule/setInvalid" }, method = RequestMethod.POST)
	PackageBean setProductRuleInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomProductRule/setDetele" }, method = RequestMethod.POST)
	PackageBean setProductRuleDetele(@RequestBody String jsonPackageBean);

	// ================================產品:負責人配置================================
	@RequestMapping(value = { "/bomKeeper/getSearch" }, method = RequestMethod.POST)
	PackageBean getKeeperSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomKeeper/getReport" }, method = RequestMethod.POST)
	PackageBean getKeeperReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomKeeper/setModify" }, method = RequestMethod.POST)
	PackageBean setKeeperModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomKeeper/setAdd" }, method = RequestMethod.POST)
	PackageBean setKeeperAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomKeeper/setInvalid" }, method = RequestMethod.POST)
	PackageBean setKeeperInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomKeeper/setDetele" }, method = RequestMethod.POST)
	PackageBean setKeeperDetele(@RequestBody String jsonPackageBean);

	// ================================紀錄-物料異動================================
	@RequestMapping(value = { "/bomHistory/getSearch" }, method = RequestMethod.POST)
	PackageBean getHistorySearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomHistory/getReport" }, method = RequestMethod.POST)
	PackageBean getHistoryReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomHistory/setModify" }, method = RequestMethod.POST)
	PackageBean setHistoryModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomHistory/setAdd" }, method = RequestMethod.POST)
	PackageBean setHistoryAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomHistory/setInvalid" }, method = RequestMethod.POST)
	PackageBean setHistoryInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomHistory/setDetele" }, method = RequestMethod.POST)
	PackageBean setHistoryDetele(@RequestBody String jsonPackageBean);

	// ================================BOM異動:主要負責人通知================================
	@RequestMapping(value = { "/bomNotification/getSearch" }, method = RequestMethod.POST)
	PackageBean getBomNotificationSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomNotification/getReport" }, method = RequestMethod.POST)
	PackageBean getBomNotificationReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomNotification/setModify" }, method = RequestMethod.POST)
	PackageBean setBomNotificationModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomNotification/setAdd" }, method = RequestMethod.POST)
	PackageBean setBomNotificationAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomNotification/setInvalid" }, method = RequestMethod.POST)
	PackageBean setBomNotificationInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/bomNotification/setDetele" }, method = RequestMethod.POST)
	PackageBean setBomNotificationDetele(@RequestBody String jsonPackageBean);

}
