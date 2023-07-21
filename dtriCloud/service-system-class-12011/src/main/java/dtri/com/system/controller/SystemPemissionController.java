package dtri.com.system.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import dtri.com.system.service.SystemPemissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class SystemPemissionController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	SystemPemissionService pemissionService;

	@RequestMapping("/getDate")
	public String getDate() {
		logger.trace("test_trace");
		logger.debug("test_debug");
		logger.info("test_info");
		logger.warn("test_warn");
		logger.error("test_error");

		String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(new Date());
	}
}
