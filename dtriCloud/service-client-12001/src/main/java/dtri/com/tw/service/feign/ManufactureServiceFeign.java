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
@FeignClient(value = "SERVICE-MANUFACTURE", path = "service-manufacture")
public interface ManufactureServiceFeign {

	// ================================通用-區域事件處理================================
	@RequestMapping(value = { "/manufactureAction/getSearch" }, method = RequestMethod.POST)
	PackageBean getActionSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/manufactureAction/getSearchDetail" }, method = RequestMethod.POST)
	PackageBean getActionSearchDetail(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/manufactureAction/setModifyNormal" }, method = RequestMethod.POST)
	PackageBean setActionModifyNormal(@RequestBody String jsonPackageBean);

	// ================================範例:通用-物料清單================================
//	@RequestMapping(value = { "/manufactureMaterial/getSearch" }, method = RequestMethod.POST)
//	PackageBean getMaterialSearch(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/manufactureMaterial/getReport" }, method = RequestMethod.POST)
//	PackageBean getMaterialReport(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/manufactureMaterial/setModify" }, method = RequestMethod.POST)
//	PackageBean setMaterialModify(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/manufactureMaterial/setAdd" }, method = RequestMethod.POST)
//	PackageBean setMaterialAdd(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/manufactureMaterial/setInvalid" }, method = RequestMethod.POST)
//	PackageBean setMaterialInvalid(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/manufactureMaterial/setDetele" }, method = RequestMethod.POST)
//	PackageBean setMaterialDetele(@RequestBody String jsonPackageBean);

}