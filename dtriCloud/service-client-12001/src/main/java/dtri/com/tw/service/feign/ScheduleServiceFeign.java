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
@FeignClient(value = "SERVICE-SCHEDULE", path = "service-schedule")
public interface ScheduleServiceFeign {
	// ================================通用-須補料單================================
	@RequestMapping(value = { "/scheduleShortageList/getSearch" }, method = RequestMethod.POST)
	PackageBean getShortageListSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageList/getReport" }, method = RequestMethod.POST)
	PackageBean getShortageListReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageList/setInvalid" }, method = RequestMethod.POST)
	PackageBean setShortageListInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageList/setModify" }, method = RequestMethod.POST)
	PackageBean setShortageListModify(@RequestBody String jsonPackageBean);

	// ================================通用-外包商排程================================
	@RequestMapping(value = { "/scheduleOutsourcer/getSearch" }, method = RequestMethod.POST)
	PackageBean getScheduleOutsourcerSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleOutsourcer/setModifySc" }, method = RequestMethod.PUT)
	PackageBean setScheduleOutsourcerModifySc(@RequestBody String jsonPackageBean);// 生管

	@RequestMapping(value = { "/scheduleOutsourcer/setModifyMc" }, method = RequestMethod.PUT)
	PackageBean setScheduleOutsourcerModifyMc(@RequestBody String jsonPackageBean);// 物控

	@RequestMapping(value = { "/scheduleOutsourcer/setModifyWm" }, method = RequestMethod.PUT)
	PackageBean setScheduleOutsourcerModifyWm(@RequestBody String jsonPackageBean);// 倉儲

	@RequestMapping(value = { "/scheduleOutsourcer/setModifyMp" }, method = RequestMethod.PUT)
	PackageBean setScheduleOutsourcerModifyMp(@RequestBody String jsonPackageBean);// 製造

	// ================================缺料通知:主要負責人通知================================
	@RequestMapping(value = { "/scheduleShortageNotification/getSearch" }, method = RequestMethod.POST)
	PackageBean getScheduleShortageNotificationSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageNotification/getReport" }, method = RequestMethod.POST)
	PackageBean getScheduleShortageNotificationReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageNotification/setModify" }, method = RequestMethod.POST)
	PackageBean setScheduleShortageNotificationModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageNotification/setAdd" }, method = RequestMethod.POST)
	PackageBean setScheduleShortageNotificationAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageNotification/setInvalid" }, method = RequestMethod.POST)
	PackageBean setScheduleShortageNotificationInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleShortageNotification/setDetele" }, method = RequestMethod.POST)
	PackageBean setScheduleShortageNotificationDetele(@RequestBody String jsonPackageBean);

	// ================================紀錄-產品製造================================
	@RequestMapping(value = { "/scheduleProductionHistory/getSearch" }, method = RequestMethod.POST)
	PackageBean getProductionHistorySearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionHistory/getReport" }, method = RequestMethod.POST)
	PackageBean getProductionHistoryReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionHistory/setModify" }, method = RequestMethod.POST)
	PackageBean setProductionHistoryModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionHistory/setAdd" }, method = RequestMethod.POST)
	PackageBean setProductionHistoryAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionHistory/setInvalid" }, method = RequestMethod.POST)
	PackageBean setProductionHistoryInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionHistory/setDetele" }, method = RequestMethod.POST)
	PackageBean setProductionHistoryDetele(@RequestBody String jsonPackageBean);

	// ================================通用-生產注意事項================================
	@RequestMapping(value = { "/scheduleProductionNotes/getSearch" }, method = RequestMethod.POST)
	PackageBean getProductionNotesSearch(@RequestBody String jsonPackageBean);
	
	@RequestMapping(value = { "/scheduleProductionNotes/getSearchOrder" }, method = RequestMethod.POST)
	PackageBean getProductionNotesSearchOrder(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionNotes/getReport" }, method = RequestMethod.POST)
	PackageBean getProductionNotesReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionNotes/setInvalid" }, method = RequestMethod.POST)
	PackageBean setProductionNotesInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/scheduleProductionNotes/setModify" }, method = RequestMethod.POST)
	PackageBean setProductionNotesModify(@RequestBody String jsonPackageBean);

}
