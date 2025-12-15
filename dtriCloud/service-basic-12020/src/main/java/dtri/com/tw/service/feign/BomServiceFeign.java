package dtri.com.tw.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @FeignClient value=微服務器名稱 <br>
 *              path=位置<br>
 *              傳遞物件固定使用JSON傳遞
 * 
 **/
@Component
@FeignClient(value = "SERVICE-BOM", path = "service-bom")
public interface BomServiceFeign {

	// 觸發
	@RequestMapping(value = { "/bomItemSpecifications/getAutoSearchTestAndUpdate" }, method = RequestMethod.POST)
	String autoSearchTestAndUpdate(@RequestBody String json);

	// 觸發
	@RequestMapping(value = { "/bomItemSpecifications/getSynBomAll" }, method = RequestMethod.POST)
	String autogetSynBomAll(@RequestBody String json);

}