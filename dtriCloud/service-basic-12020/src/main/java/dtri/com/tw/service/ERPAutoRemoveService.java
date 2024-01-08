package dtri.com.tw.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;

@Service
public class ERPAutoRemoveService {

	// 入料類-移除
	public BasicIncomingList incomingAuto(BasicIncomingList o) {
		if (o.getBilfuser().equals("")) {
			o.setBilfuser("ERP_Remove(Auto)");
			o.setBilfdate(new Date());
		}
		o.setCheckrm(false);
		o.setSysmuser("ERP_Remove(Auto)");
		o.setSysmdate(new Date());

		return o;
	}

	// 領料類-移除
	public BasicShippingList shippingAuto(BasicShippingList o) {
		if (o.getBslfuser().equals("")) {
			o.setBslfuser("ERP_Remove(Auto)");
			o.setBslfdate(new Date());
		}
		o.setCheckrm(false);
		o.setSysmuser("ERP_Remove(Auto)");
		o.setSysmdate(new Date());
		return o;
	}
	// 指另類-移除
	public BasicIncomingList commandAuto(BasicIncomingList o) {
		if (o.getBilfuser().equals("")) {
			o.setBilfuser("ERP_Remove(Auto)");
			o.setBilfdate(new Date());
		}
		o.setCheckrm(false);
		o.setSysmuser("ERP_Remove(Auto)");
		o.setSysmdate(new Date());

		return o;
	}

}
