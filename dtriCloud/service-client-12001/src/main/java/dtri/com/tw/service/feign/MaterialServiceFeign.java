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
@FeignClient(value = "SERVICE-MATERIAL", path = "service-material")
public interface MaterialServiceFeign {
	// ================================產品:軟硬體版本================================
	@RequestMapping(value = { "/materialReplacement/getSearch" }, method = RequestMethod.POST)
	PackageBean getMaterialReplacementSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialReplacement/getReport" }, method = RequestMethod.POST)
	PackageBean getMaterialReplacementReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialReplacement/setModify" }, method = RequestMethod.POST)
	PackageBean setMaterialReplacementModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialReplacement/setAdd" }, method = RequestMethod.POST)
	PackageBean setMaterialReplacementAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialReplacement/setInvalid" }, method = RequestMethod.POST)
	PackageBean setMaterialReplacementInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialReplacement/setDetele" }, method = RequestMethod.POST)
	PackageBean setMaterialReplacementDetele(@RequestBody String jsonPackageBean);

	
}
