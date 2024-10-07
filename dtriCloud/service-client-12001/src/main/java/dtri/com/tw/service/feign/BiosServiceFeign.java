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
@FeignClient(value = "SERVICE-BIOS", path = "service-bios")
public interface BiosServiceFeign {

	// ================================BIOS:版本生命樹================================
	@RequestMapping(value = { "/biosLifeCycle/getSearch" }, method = RequestMethod.POST)
	PackageBean getBiosLifeCycleSearch(@RequestBody String jsonPackageBean);

	// ================================BIOS:版本管理================================
	@RequestMapping(value = { "/biosVersion/getSearch" }, method = RequestMethod.POST)
	PackageBean getBiosVersionSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosVersion/getReport" }, method = RequestMethod.POST)
	PackageBean getBiosVersionReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosVersion/setModify" }, method = RequestMethod.POST)
	PackageBean setBiosVersionModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosVersion/setAdd" }, method = RequestMethod.POST)
	PackageBean setBiosVersionAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosVersion/setInvalid" }, method = RequestMethod.POST)
	PackageBean setBiosVersionInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosVersion/setDetele" }, method = RequestMethod.POST)
	PackageBean setBiosVersionDetele(@RequestBody String jsonPackageBean);

	// ================================BIOS:主要負責人通知================================
	@RequestMapping(value = { "/biosNotification/getSearch" }, method = RequestMethod.POST)
	PackageBean getBiosNotificationSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosNotification/getReport" }, method = RequestMethod.POST)
	PackageBean getBiosNotificationReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosNotification/setModify" }, method = RequestMethod.POST)
	PackageBean setBiosNotificationModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosNotification/setAdd" }, method = RequestMethod.POST)
	PackageBean setBiosNotificationAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosNotification/setInvalid" }, method = RequestMethod.POST)
	PackageBean setBiosNotificationInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosNotification/setDetele" }, method = RequestMethod.POST)
	PackageBean setBiosNotificationDetele(@RequestBody String jsonPackageBean);

	// ================================BIOS:BIOS顧客標記================================
	@RequestMapping(value = { "/biosCustomerTag/getSearch" }, method = RequestMethod.POST)
	PackageBean getBiosCustomerTagSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosCustomerTag/getReport" }, method = RequestMethod.POST)
	PackageBean getBiosCustomerTagReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosCustomerTag/setModify" }, method = RequestMethod.POST)
	PackageBean setBiosCustomerTagModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosCustomerTag/setAdd" }, method = RequestMethod.POST)
	PackageBean setBiosCustomerTagAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosCustomerTag/setInvalid" }, method = RequestMethod.POST)
	PackageBean setBiosCustomerTagInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/biosCustomerTag/setDetele" }, method = RequestMethod.POST)
	PackageBean setBiosCustomerTagDetele(@RequestBody String jsonPackageBean);

}
