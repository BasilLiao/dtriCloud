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

	// ================================範例:通用-流程卡管理================================
	@RequestMapping(value = { "/manufactureProcessCard/setDetele" }, method = RequestMethod.POST)
	PackageBean setProcessCardDetele(String beanToJson);

	@RequestMapping(value = { "/manufactureProcessCard/setInvalid" }, method = RequestMethod.POST)
	PackageBean setProcessCardInvalid(String beanToJson);

	@RequestMapping(value = { "/manufactureProcessCard/setModify" }, method = RequestMethod.POST)
	PackageBean setProcessCardModify(String beanToJson);

	@RequestMapping(value = { "/manufactureProcessCard/setAdd" }, method = RequestMethod.POST)
	PackageBean setProcessCardAdd(String beanToJson);

	@RequestMapping(value = { "/manufactureProcessCard/getReport" }, method = RequestMethod.POST)
	PackageBean getProcessCardReport(String beanToJson);

	@RequestMapping(value = { "/manufactureProcessCard/getSearch" }, method = RequestMethod.POST)
	PackageBean getProcessCardSearch(String beanToJson);

	// ================================範例:通用-產品SN規則================================
	@RequestMapping(value = { "/manufactureRuleNumber/setDetele" }, method = RequestMethod.POST)
	PackageBean setRuleNumberDetele(String beanToJson);

	@RequestMapping(value = { "/manufactureRuleNumber/setInvalid" }, method = RequestMethod.POST)
	PackageBean setRuleNumberInvalid(String beanToJson);

	@RequestMapping(value = { "/manufactureRuleNumber/setModify" }, method = RequestMethod.POST)
	PackageBean setRuleNumberModify(String beanToJson);

	@RequestMapping(value = { "/manufactureRuleNumber/setAdd" }, method = RequestMethod.POST)
	PackageBean setRuleNumberAdd(String beanToJson);

	@RequestMapping(value = { "/manufactureRuleNumber/getReport" }, method = RequestMethod.POST)
	PackageBean getRuleNumberReport(String beanToJson);

	@RequestMapping(value = { "/manufactureRuleNumber/getSearch" }, method = RequestMethod.POST)
	PackageBean getRuleNumberSearch(String beanToJson);

	// ================================範例:通用-產品SN清單================================
	@RequestMapping(value = { "/manufactureSerialNumber/setDetele" }, method = RequestMethod.POST)
	PackageBean setSerialNumberDetele(String beanToJson);

	@RequestMapping(value = { "/manufactureSerialNumber/setInvalid" }, method = RequestMethod.POST)
	PackageBean setSerialNumberInvalid(String beanToJson);

	@RequestMapping(value = { "/manufactureSerialNumber/setModify" }, method = RequestMethod.POST)
	PackageBean setSerialNumberModify(String beanToJson);

	@RequestMapping(value = { "/manufactureSerialNumber/setAdd" }, method = RequestMethod.POST)
	PackageBean setSerialNumberAdd(String beanToJson);

	@RequestMapping(value = { "/manufactureSerialNumber/getReport" }, method = RequestMethod.POST)
	PackageBean getSerialNumberReport(String beanToJson);

	@RequestMapping(value = { "/manufactureSerialNumber/getSearch" }, method = RequestMethod.POST)
	PackageBean getSerialNumberSearch(String beanToJson);

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