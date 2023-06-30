package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.service.CloudExceptionService;
import dtri.com.tw.service.IndexService;
import dtri.com.tw.service.PackageService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class IndexController extends AbstractController {

	@Autowired
	private IndexService indexService;
	@Autowired
	private PackageService packageService;

	/**
	 * 登入 and 登出-畫面
	 */
	@RequestMapping(value = { "/", "/login.basil", "/index.basil", "/logout.basil" }, method = { RequestMethod.GET })
	public String loginCheck(HttpServletRequest request, Model model) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());

		// 可能有錯誤碼
		String error = request.getParameter("status");
		String language = request.getParameter("lg");
		model.addAttribute("status", error);
		model.addAttribute("lg", language);

		// 回傳-模板
		return "./html/login.html";
	}

	/**
	 * (初始化)主頁
	 * 
	 */
	@RequestMapping(value = { "/main.basil" }, method = { RequestMethod.POST })
	public ModelAndView indexCheck() {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		String packageJson = "{}";
		try {
			PackageBean packageBean = new PackageBean();
			// Step0. 取得令牌=>
			indexService.getToken();
			// Step1.解包=>轉換 PackageBean=>檢查=>Pass

			// Step2.執行=>服務項目
			indexService.getMenu(packageBean, loginUser().getSystemUser());
			// Step3.打包=>轉換 PackageBean=>包裝=>Json
			packageJson = packageService.beanToJson(packageBean);
		} catch (CloudExceptionService ex) {
			// Step4-1. 已知-故障回報
			loggerWarn(ex.toString());
			ex.getErrorCodeMessage();
		} catch (Exception e) {
			// Step4-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
		}
		// Step4. 回傳-模板
		return new ModelAndView("./html/main.html", "initMain", packageJson);
	}

	/**
	 * (再次讀取)主頁
	 */
	@ResponseBody
	@RequestMapping(value = { "ajax/index.basil" }, method = { RequestMethod.POST })
	public String index(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務項目
			// Step3.打包=>轉換 PackageBean=>包裝=>Json
			packageJson = packageService.beanToJson(packageBean);
		} catch (Exception e) {
			// Step4-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
		}
		// 回傳-模板
		return packageJson;
	}

	@Override
	String access(String jsonObject) {
		return null;
	}

	@Override
	String search(String jsonObject) {
		return null;
	}

	@Override
	String modify(String jsonObject) {
		return null;
	}

	@Override
	String delete(String jsonObject) {
		return null;
	}
}
