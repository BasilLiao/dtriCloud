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

}
