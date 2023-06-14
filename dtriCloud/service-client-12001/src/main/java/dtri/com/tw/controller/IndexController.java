package dtri.com.tw.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;

import dtri.com.tw.login.CustomerUserDetails;
import dtri.com.tw.login.JwtUtilities;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@Controller
public class IndexController {

	// 功能
	final static String SYS_F = "index.basil";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtilities jwtUtilities;

	/**
	 * 登入 and 登出-畫面
	 */
	@RequestMapping(value = { "/", "/login.basil", "/index.basil", "/logout.basil" }, method = { RequestMethod.GET })
	public String loginCheck(HttpServletRequest request, Model model) {
		logger.trace("loginCheck");

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
		logger.trace("indexCheck");

		// Step1.取得使用者
		List<String> rolesGroup = new ArrayList<String>();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomerUserDetails userDetails = new CustomerUserDetails();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			userDetails = (CustomerUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}
		// Step2.權限清單
		userDetails.getSystemGroup().forEach(systemGroup -> {
			String role = systemGroup.getSystemPermission().getSpcontrol().replaceAll("\\.", "_") //
					+ "_"//
					+ systemGroup.getSystemPermission().getSppermission();
			rolesGroup.add(role);
		});
		// Step3.寫入(令牌)token
		/*
		 * authentication = authenticationManager .authenticate(new
		 * UsernamePasswordAuthenticationToken(userDetails.getUsername(),
		 * userDetails.getPassword()));
		 * SecurityContextHolder.getContext().setAuthentication(authentication);
		 */
		// Step4.建立(令牌)token 憑證
		String token = jwtUtilities.generateToken(userDetails.getUsername(), rolesGroup);
		JsonObject innerObject = new JsonObject();
		innerObject.addProperty("token", token);
		// 回傳-模板
		return new ModelAndView("./html/main.html", "initMain", innerObject.toString());
	}
}
