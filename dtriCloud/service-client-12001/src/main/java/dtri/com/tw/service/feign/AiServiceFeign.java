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
@FeignClient(value = "SERVICE-AI", path = "service-ai", configuration = FeignConfig.class)
public interface AiServiceFeign {

	// ================================AI對話機制================================
	@RequestMapping(value = { "aiChat/getSearch" }, method = RequestMethod.POST)
	PackageBean getAiChatSearch(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/aiChat/getReport" }, method = RequestMethod.POST)
	PackageBean getAiChatReport(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/aiChat/setModify" }, method = RequestMethod.POST)
	PackageBean setAiChatModify(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/aiChat/setAdd" }, method = RequestMethod.POST)
	PackageBean setAiChatAdd(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/aiChat/setInvalid" }, method = RequestMethod.POST)
	PackageBean setAiChatInvalid(@RequestBody String jsonPackageBean);

	@RequestMapping(value = { "/aiChat/setDetele" }, method = RequestMethod.POST)
	PackageBean setAiChatDetele(@RequestBody String jsonPackageBean);

}
