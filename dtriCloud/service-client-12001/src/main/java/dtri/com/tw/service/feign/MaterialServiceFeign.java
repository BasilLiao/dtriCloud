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

	@RequestMapping(value = { "/materialShortage/getSearch" }, method = RequestMethod.POST)
	PackageBean getMaterialShortageSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialShortage/getItemStock" }, method = RequestMethod.POST)
	PackageBean getMaterialShortageItemStock(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialEol/getSearch" }, method = RequestMethod.POST)
	PackageBean getMaterialEolSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialEol/getReport" }, method = RequestMethod.POST)
	PackageBean getMaterialEolReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialEol/getInitSearchData" }, method = RequestMethod.POST)
	PackageBean getMaterialEolInitSearchData(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/musUserSearch/setMusUserSearch" }, method = RequestMethod.POST)
	PackageBean setMusUserSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialProcess/getSearch" }, method = RequestMethod.POST)
	PackageBean getMaterialProcessSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialProcess/setModify" }, method = RequestMethod.POST)
	PackageBean setMaterialProcessModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialProcess/setDelete" }, method = RequestMethod.POST)
	PackageBean setMaterialProcessDelete(@RequestBody String jsonPackageBean);

	// ================================虛擬專案:產品系列主建================================
	@RequestMapping(value = { "/materialVirtualProject/getSearch" }, method = RequestMethod.POST)
	PackageBean getMaterialVirtualProjectSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/setModify" }, method = RequestMethod.POST)
	PackageBean setMaterialVirtualProjectModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/setAdd" }, method = RequestMethod.POST)
	PackageBean setMaterialVirtualProjectAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/setDelete" }, method = RequestMethod.POST)
	PackageBean setMaterialVirtualProjectDelete(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/getReport" }, method = RequestMethod.POST)
	PackageBean getMaterialVirtualProjectReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/simulate" }, method = RequestMethod.POST)
	PackageBean simulateMaterialVirtualProject(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/otherProjects" }, method = RequestMethod.POST)
	PackageBean getMaterialVirtualProjectOtherProjects(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/materialVirtualProject/getInitSearchData" }, method = RequestMethod.POST)
	PackageBean getMaterialVirtualProjectInitSearchData(@RequestBody String jsonPackageBean);
}
