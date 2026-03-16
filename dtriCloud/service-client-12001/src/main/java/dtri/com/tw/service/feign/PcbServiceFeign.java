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
@FeignClient(value = "SERVICE-PURCHASE", path = "service-purchase")
public interface PcbServiceFeign {
	// ================================管理-PCB資料管理(暫時掛載在採購下方)================================
	@RequestMapping(value = { "/pcbConfigSettings/getSearch" }, method = RequestMethod.POST)
	PackageBean getConfigSettingsSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/pcbConfigSettings/getReport" }, method = RequestMethod.POST)
	PackageBean getConfigSettingsReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/pcbConfigSettings/setModify" }, method = RequestMethod.POST)
	PackageBean setConfigSettingsModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/pcbConfigSettings/setAdd" }, method = RequestMethod.POST)
	PackageBean setConfigSettingsAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/pcbConfigSettings/setInvalid" }, method = RequestMethod.POST)
	PackageBean setConfigSettingsInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/pcbConfigSettings/setDetele" }, method = RequestMethod.POST)
	PackageBean setConfigSettingsDetele(@RequestBody String jsonPackageBean);

}