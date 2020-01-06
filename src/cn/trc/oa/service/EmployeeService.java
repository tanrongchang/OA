package cn.trc.oa.service;

import java.util.List;

import cn.trc.oa.pojo.Employee;
import cn.trc.oa.pojo.SysRole;

public interface EmployeeService {

	public Employee findEmployeeByName(String username);
	
	public Employee findEmpById(Long id);

	public List<SysRole> findAllRole();

	public void updateEmployeeRole(String roleId, String userId);

	public List<Employee> findEmployeeByManagerId(int managerId);

	public void saveUser(Employee user);


}
