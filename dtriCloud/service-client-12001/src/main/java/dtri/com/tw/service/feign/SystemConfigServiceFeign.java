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
public interface SystemConfigServiceFeign {

	@RequestMapping(value = { "/systemConfig/getSearch" }, method = RequestMethod.POST)
	PackageBean getSearch(@RequestBody String jsonPackageBean);
	
	@RequestMapping(value = { "/systemConfig/getReport" }, method = RequestMethod.POST)
	PackageBean getReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setModify" }, method = RequestMethod.POST)
	PackageBean setModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setAdd" }, method = RequestMethod.POST)
	PackageBean setAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setInvalid" }, method = RequestMethod.POST)
	PackageBean setInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/systemConfig/setDetele" }, method = RequestMethod.POST)
	PackageBean setDetele(@RequestBody String jsonPackageBean);

}
