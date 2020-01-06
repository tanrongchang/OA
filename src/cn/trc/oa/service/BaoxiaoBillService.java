package cn.trc.oa.service;

import java.util.List;

import cn.trc.oa.pojo.Baoxiaobill;

public interface BaoxiaoBillService {

	List<Baoxiaobill> findBaoxiaoBillListByUserId(Long userid);

	Baoxiaobill findBillById(long id);

	void deleteBillById(long billId);
	
}
