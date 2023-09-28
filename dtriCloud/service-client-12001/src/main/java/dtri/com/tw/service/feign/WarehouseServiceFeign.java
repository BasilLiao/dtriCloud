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
@FeignClient(value = "SERVICE-WAREHOUSE", path = "service-warehouse")
public interface WarehouseServiceFeign {
	// ================================管理-單據分配處理================================
	@RequestMapping(value = { "/warehouseAssignment/getSearch" }, method = RequestMethod.POST)
	PackageBean getAssignmentSearch(@RequestBody String jsonPackageBean);

//	@RequestMapping(value = { "/warehouseAssignment/getReport" }, method = RequestMethod.POST)
//	PackageBean getAssignmentReport(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAssignment/setModify" }, method = RequestMethod.POST)
//	PackageBean setAssignmentModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseAssignment/setModifyAgree" }, method = RequestMethod.POST)
	PackageBean setAssignmentModifyAgree(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseAssignment/setModifyPassAll" }, method = RequestMethod.POST)
	PackageBean setAssignmentModifyPassAll(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseAssignment/setModifyReturnAll" }, method = RequestMethod.POST)
	PackageBean setAssignmentModifyReturnAll(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseAssignment/setModifyUrgency" }, method = RequestMethod.POST)
	PackageBean setAssignmentModifyUrgency(@RequestBody String jsonPackageBean);

//	@RequestMapping(value = { "/warehouseAssignment/setAdd" }, method = RequestMethod.POST)
//	PackageBean setAssignmentAdd(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAssignment/setInvalid" }, method = RequestMethod.POST)
//	PackageBean setAssignmentInvalid(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAssignment/setDetele" }, method = RequestMethod.POST)
//	PackageBean setAssignmentDetele(@RequestBody String jsonPackageBean);

	// ================================通用-區域事件處理================================
	@RequestMapping(value = { "/warehouseAction/getSearch" }, method = RequestMethod.POST)
	PackageBean getActionSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseAction/setModifyNormal" }, method = RequestMethod.POST)
	PackageBean setActionModifyNormal(@RequestBody String jsonPackageBean);

//	@RequestMapping(value = { "/warehouseAction/getReport" }, method = RequestMethod.POST)
//	PackageBean getActionReport(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAction/setModify" }, method = RequestMethod.POST)
//	PackageBean setActionModify(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAction/setAdd" }, method = RequestMethod.POST)
//	PackageBean setActionAdd(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAction/setInvalid" }, method = RequestMethod.POST)
//	PackageBean setActionInvalid(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseAction/setDetele" }, method = RequestMethod.POST)
//	PackageBean setActionDetele(@RequestBody String jsonPackageBean);

	// ================================管理-庫存同步================================
	@RequestMapping(value = { "/warehouseSynchronize/getSearch" }, method = RequestMethod.POST)
	PackageBean getSynchronizeSearch(@RequestBody String jsonPackageBean);

//	@RequestMapping(value = { "/warehouseSynchronize/getReport" }, method = RequestMethod.POST)
//	PackageBean getSynchronizeReport(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseSynchronize/setModify" }, method = RequestMethod.POST)
//	PackageBean setSynchronizeModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseSynchronize/setModifySynchronizeQty" }, method = RequestMethod.POST)
	PackageBean setModifySynchronizeQty(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseSynchronize/setModifySynchronizeItem" }, method = RequestMethod.POST)
	PackageBean setModifySynchronizeItem(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseSynchronize/setModifySynchronizeRemove" }, method = RequestMethod.POST)
	PackageBean setModifySynchronizeRemove(@RequestBody String jsonPackageBean);

//	@RequestMapping(value = { "/warehouseSynchronize/setAdd" }, method = RequestMethod.POST)
//	PackageBean setSynchronizeAdd(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseSynchronize/setInvalid" }, method = RequestMethod.POST)
//	PackageBean setSynchronizeInvalid(@RequestBody String jsonPackageBean);
//
//	@RequestMapping(value = { "/warehouseSynchronize/setDetele" }, method = RequestMethod.POST)
//	PackageBean setSynchronizeDetele(@RequestBody String jsonPackageBean);

	// ================================紀錄-物料異動================================
	@RequestMapping(value = { "/warehouseHistory/getSearch" }, method = RequestMethod.POST)
	PackageBean getHistorySearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseHistory/getReport" }, method = RequestMethod.POST)
	PackageBean getHistoryReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseHistory/setModify" }, method = RequestMethod.POST)
	PackageBean setHistoryModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseHistory/setAdd" }, method = RequestMethod.POST)
	PackageBean setHistoryAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseHistory/setInvalid" }, method = RequestMethod.POST)
	PackageBean setHistoryInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseHistory/setDetele" }, method = RequestMethod.POST)
	PackageBean setHistoryDetele(@RequestBody String jsonPackageBean);

	// ================================設定-區域負責人================================
	@RequestMapping(value = { "/warehouseKeeper/getSearch" }, method = RequestMethod.POST)
	PackageBean getKeeperSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseKeeper/getReport" }, method = RequestMethod.POST)
	PackageBean getKeeperReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseKeeper/setModify" }, method = RequestMethod.POST)
	PackageBean setKeeperModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseKeeper/setAdd" }, method = RequestMethod.POST)
	PackageBean setKeeperAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseKeeper/setInvalid" }, method = RequestMethod.POST)
	PackageBean setKeeperInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseKeeper/setDetele" }, method = RequestMethod.POST)
	PackageBean setKeeperDetele(@RequestBody String jsonPackageBean);

	// ================================設定-單據濾器================================
	@RequestMapping(value = { "/warehouseTypeFilter/getSearch" }, method = RequestMethod.POST)
	PackageBean getTypeFilterSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseTypeFilter/getReport" }, method = RequestMethod.POST)
	PackageBean getTypeFilterReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseTypeFilter/setModify" }, method = RequestMethod.POST)
	PackageBean setTypeFilterModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseTypeFilter/setAdd" }, method = RequestMethod.POST)
	PackageBean setTypeFilterAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseTypeFilter/setInvalid" }, method = RequestMethod.POST)
	PackageBean setTypeFilterInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseTypeFilter/setDetele" }, method = RequestMethod.POST)
	PackageBean setTypeFilterDetele(@RequestBody String jsonPackageBean);

	// ================================設定-倉儲濾器================================
	@RequestMapping(value = { "/warehouseConfig/getSearch" }, method = RequestMethod.POST)
	PackageBean getConfigSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseConfig/getReport" }, method = RequestMethod.POST)
	PackageBean getConfigReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseConfig/setModify" }, method = RequestMethod.POST)
	PackageBean setConfigModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseConfig/setAdd" }, method = RequestMethod.POST)
	PackageBean setConfigAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseConfig/setInvalid" }, method = RequestMethod.POST)
	PackageBean setConfigInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseConfig/setDetele" }, method = RequestMethod.POST)
	PackageBean setConfigDetele(@RequestBody String jsonPackageBean);

	// ================================通用-區域清單================================
	@RequestMapping(value = { "/warehouseArea/getSearch" }, method = RequestMethod.POST)
	PackageBean getAreaSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseArea/getReport" }, method = RequestMethod.POST)
	PackageBean getAreaReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseArea/setModify" }, method = RequestMethod.POST)
	PackageBean setAreaModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseArea/setAdd" }, method = RequestMethod.POST)
	PackageBean setAreaAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseArea/setInvalid" }, method = RequestMethod.POST)
	PackageBean setAreaInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseArea/setDetele" }, method = RequestMethod.POST)
	PackageBean setAreaDetele(@RequestBody String jsonPackageBean);

	// ================================通用-物料清單================================
	@RequestMapping(value = { "/warehouseMaterial/getSearch" }, method = RequestMethod.POST)
	PackageBean getMaterialSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseMaterial/getReport" }, method = RequestMethod.POST)
	PackageBean getMaterialReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseMaterial/setModify" }, method = RequestMethod.POST)
	PackageBean setMaterialModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseMaterial/setAdd" }, method = RequestMethod.POST)
	PackageBean setMaterialAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseMaterial/setInvalid" }, method = RequestMethod.POST)
	PackageBean setMaterialInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/warehouseMaterial/setDetele" }, method = RequestMethod.POST)
	PackageBean setMaterialDetele(@RequestBody String jsonPackageBean);

}