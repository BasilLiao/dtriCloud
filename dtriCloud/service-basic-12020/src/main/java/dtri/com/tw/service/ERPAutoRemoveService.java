package dtri.com.tw.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;

@Service
public class ERPAutoRemoveService {
	
	// 入料類-移除
	public BasicIncomingList incomingAuto(BasicIncomingList o) {
		o.setBilfuser("ERP_Remove(Auto)");
		o.setBilfdate(new Date());
		o.setSysmuser("ERP_Remove(Auto)");
		o.setSysmdate(new Date());
		o.setBilpngqty(0);
		o.setBilpnqty(0);
		return o;
	}

	// 領料類-移除
	public BasicShippingList shippingAuto(BasicShippingList o) {
		o.setBslfuser("ERP_Remove(Auto)");
		o.setBslfdate(new Date());
		o.setSysmuser("ERP_Remove(Auto)");
		o.setSysmdate(new Date());
		o.setBslpngqty(0);
		o.setBslpnqty(0);
		return o;
	}

}
