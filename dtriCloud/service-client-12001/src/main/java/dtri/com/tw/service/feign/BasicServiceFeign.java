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
	// ================================同步:重新同步單據================================
	@RequestMapping(value = { "/basicSynchronize/getReSynchronize" }, method = RequestMethod.POST)
	PackageBean getReSynchronizeDocument(@RequestBody String jsonPackageBean);

	// ================================同步:產品機型================================
	@RequestMapping(value = { "/basicProductModel/getSearch" }, method = RequestMethod.POST)
	PackageBean getProductModelSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicProductModel/getReport" }, method = RequestMethod.POST)
	PackageBean getProductModelReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicProductModel/setModify" }, method = RequestMethod.POST)
	PackageBean setProductModelModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicProductModel/setAdd" }, method = RequestMethod.POST)
	PackageBean setProductModelAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicProductModel/setInvalid" }, method = RequestMethod.POST)
	PackageBean setProductModelInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicProductModel/setDetele" }, method = RequestMethod.POST)
	PackageBean setProductModelDetele(@RequestBody String jsonPackageBean);

	// ================================一般:寄信件清單================================
	@RequestMapping(value = { "/basicNotificationMail/getSearch" }, method = RequestMethod.POST)
	PackageBean getNotificationMailSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicNotificationMail/getReport" }, method = RequestMethod.POST)
	PackageBean getNotificationMailReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicNotificationMail/setModify" }, method = RequestMethod.POST)
	PackageBean setNotificationMailModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicNotificationMail/setAdd" }, method = RequestMethod.POST)
	PackageBean setNotificationMailAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicNotificationMail/setInvalid" }, method = RequestMethod.POST)
	PackageBean setNotificationMailInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicNotificationMail/setDetele" }, method = RequestMethod.POST)
	PackageBean setNotificationMailDetele(@RequestBody String jsonPackageBean);

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

	// ================================同步:BOM組成================================
	@RequestMapping(value = { "/basicBomIngredients/getSearch" }, method = RequestMethod.POST)
	PackageBean getBomIngredientsSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicBomIngredients/getReport" }, method = RequestMethod.POST)
	PackageBean getBomIngredientsReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicBomIngredients/setModify" }, method = RequestMethod.POST)
	PackageBean setBomIngredientsModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicBomIngredients/setAdd" }, method = RequestMethod.POST)
	PackageBean setBomIngredientsAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicBomIngredients/setInvalid" }, method = RequestMethod.POST)
	PackageBean setBomIngredientsInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/basicBomIngredients/setDetele" }, method = RequestMethod.POST)
	PackageBean setBomIngredientsDetele(@RequestBody String jsonPackageBean);

}
