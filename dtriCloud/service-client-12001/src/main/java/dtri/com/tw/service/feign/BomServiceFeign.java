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

}
