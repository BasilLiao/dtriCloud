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
@FeignClient(value = "SERVICE-CLIENT", path = "DTRcloud")
public interface ClientServiceFeign {

	// 觸發
	@RequestMapping(value = { "/websocket/schedule_outsourcer_service" }, method = RequestMethod.POST)
	String setOutsourcerSynchronizeCell(@RequestBody String json);

	// 觸發
	@RequestMapping(value = { "/websocket/schedule_infactory_service" }, method = RequestMethod.POST)
	String setInfactorySynchronizeCell(@RequestBody String json);

	// 觸發
	@RequestMapping(value = { "/websocket/schedule_infactory_dft_service" }, method = RequestMethod.POST)
	PackageBean setInfactorySynchronizeDftCell(@RequestBody String json);

}