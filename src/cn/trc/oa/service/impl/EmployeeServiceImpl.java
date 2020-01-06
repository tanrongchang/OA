package cn.trc.oa.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.trc.oa.mapper.EmployeeMapper;
import cn.trc.oa.mapper.SysRoleMapper;
import cn.trc.oa.mapper.SysUserRoleMapper;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.pojo.EmployeeExample;
import cn.trc.oa.pojo.SysRole;
import cn.trc.oa.pojo.SysUserRole;
import cn.trc.oa.pojo.SysUserRoleExample;
import cn.trc.oa.service.EmployeeService;



@Service("employeeService")
public class EmployeeServiceImpl implements EmployeeService {
	
	@Autowired
	private EmployeeMapper employeeMapper;
	@Autowired
	private SysRoleMapper sysRoleMapper;
	@Autowired
	private SysUserRoleMapper userRoleMapper;
	
	@Override//假设用户名是唯一
	public Employee findEmployeeByName(String username) {
		EmployeeExample example = new EmployeeExample();
		EmployeeExample.Criteria criteria = example.createCriteria();
		
		criteria.andNameEqualTo(username);
		List<Employee> list = employeeMapper.selectByExample(example);
		
		if(list!=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

	@Override
	public Employee findEmpById(Long id) {
		return employeeMapper.selectByPrimaryKey(id);
	}

	@Override
	public List<SysRole> findAllRole() {
		return sysRoleMapper.selectByExample(null);
	}

	@Override
	public void updateEmployeeRole(String roleId, String userId) {
		SysUserRoleExample example = new SysUserRoleExample();
		SysUserRoleExample.Criteria criteria = example.createCriteria();
		criteria.andSysUserIdEqualTo(userId);
		
		SysUserRole userRole = userRoleMapper.selectByExample(example).get(0);
		userRole.setSysRoleId(roleId);
		
		
		userRoleMapper.updateByPrimaryKey(userRole);
		
	}

	@Override
	public List<Employee> findEmployeeByManagerId(int managerId) {
		EmployeeExample example = new EmployeeExample();
		EmployeeExample.Criteria criteria = example.createCriteria();
		criteria.andRoleEqualTo(managerId);
		List<Employee> list = employeeMapper.selectByExample(example);
		return list;
	}

	@Override
	public void saveUser(Employee user) {
		employeeMapper.insert(user);
		SysUserRole userRole = new SysUserRole();
		List<SysUserRole> list = userRoleMapper.selectByExample(null);
		int size = list.size()+1;
		userRole.setId(String.valueOf(size));
		userRole.setSysUserId(user.getName());
		userRole.setSysRoleId(String.valueOf(user.getRole()));
		userRoleMapper.insert(userRole);
		
		
	}


}
