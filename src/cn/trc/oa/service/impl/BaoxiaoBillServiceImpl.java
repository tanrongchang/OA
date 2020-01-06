package cn.trc.oa.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.trc.oa.mapper.BaoxiaobillMapper;
import cn.trc.oa.pojo.Baoxiaobill;
import cn.trc.oa.pojo.BaoxiaobillExample;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.service.BaoxiaoBillService;

@Service
public class BaoxiaoBillServiceImpl implements BaoxiaoBillService {
	
	@Autowired
	private BaoxiaobillMapper baoxiaoMapper;
	
	@Override
	public List<Baoxiaobill> findBaoxiaoBillListByUserId(Long userid) {
		BaoxiaobillExample baoxiaoExample = new BaoxiaobillExample();
		BaoxiaobillExample.Criteria criteria = baoxiaoExample.createCriteria();
		criteria.andUserIdEqualTo(userid);
		
		List<Baoxiaobill> list = baoxiaoMapper.selectByExample(baoxiaoExample);;
		
		if(list!=null && list.size()>0){
			return list;
		}
		return null;
	}

	@Override
	public Baoxiaobill findBillById(long id) {
		return baoxiaoMapper.selectByPrimaryKey(id);
	}

	@Override
	public void deleteBillById(long billId) {
		baoxiaoMapper.deleteByPrimaryKey(billId);
	}

}
