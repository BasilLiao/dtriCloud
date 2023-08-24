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
@FeignClient(value = "SERVICE-BASIC", path = "service-basic")
public interface BasicServiceFeign {
	// ================================同步:入料單據================================
	@RequestMapping(value = { "/basicIncomingList/getSearch" }, method = RequestMethod.POST)
	PackageBean getIncomingListSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicIncomingList/getReport" }, method = RequestMethod.POST)
	PackageBean getIncomingListReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicIncomingList/setModify" }, method = RequestMethod.POST)
	PackageBean setIncomingListModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicIncomingList/setAdd" }, method = RequestMethod.POST)
	PackageBean setIncomingListAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicIncomingList/setInvalid" }, method = RequestMethod.POST)
	PackageBean setIncomingListInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicIncomingList/setDetele" }, method = RequestMethod.POST)
	PackageBean setIncomingListDetele(@RequestBody String jsonPackageBean);

	// ================================同步:領料單據================================
	@RequestMapping(value = { "/basicShippingList/getSearch" }, method = RequestMethod.POST)
	PackageBean getShippingListSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicShippingList/getReport" }, method = RequestMethod.POST)
	PackageBean getShippingListReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicShippingList/setModify" }, method = RequestMethod.POST)
	PackageBean setShippingListModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicShippingList/setAdd" }, method = RequestMethod.POST)
	PackageBean setShippingListAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicShippingList/setInvalid" }, method = RequestMethod.POST)
	PackageBean setShippingListInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicShippingList/setDetele" }, method = RequestMethod.POST)
	PackageBean setShippingListDetele(@RequestBody String jsonPackageBean);

	// ================================同步:指令單據================================
	@RequestMapping(value = { "/basicCommandList/getSearch" }, method = RequestMethod.POST)
	PackageBean getCommandListSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicCommandList/getReport" }, method = RequestMethod.POST)
	PackageBean getCommandListReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicCommandList/setModify" }, method = RequestMethod.POST)
	PackageBean setCommandListModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicCommandList/setAdd" }, method = RequestMethod.POST)
	PackageBean setCommandListAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicCommandList/setInvalid" }, method = RequestMethod.POST)
	PackageBean setCommandListInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicCommandList/setDetele" }, method = RequestMethod.POST)
	PackageBean setCommandListDetele(@RequestBody String jsonPackageBean);

}
