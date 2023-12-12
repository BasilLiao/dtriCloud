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

}
