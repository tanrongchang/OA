package cn.trc.oa.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.trc.oa.exception.CustomException;
import cn.trc.oa.mapper.EmployeeMapper;
import cn.trc.oa.mapper.SysPermissionMapper;
import cn.trc.oa.mapper.SysPermissionMapperCustom;
import cn.trc.oa.mapper.SysRoleMapper;
import cn.trc.oa.mapper.SysRolePermissionMapper;
import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.pojo.EmployeeExample;
import cn.trc.oa.pojo.MenuTree;
import cn.trc.oa.pojo.SysPermission;
import cn.trc.oa.pojo.SysPermissionExample;
import cn.trc.oa.pojo.SysRole;
import cn.trc.oa.pojo.SysRolePermission;
import cn.trc.oa.pojo.SysRolePermissionExample;
import cn.trc.oa.service.SysService;
import cn.trc.oa.utils.MD5;





@Service
public class SysServiceImpl implements SysService {
	
	@Autowired
	private SysRolePermissionMapper rolePermissionMapper;
	@Autowired
	private SysRoleMapper roleMapper;
	@Autowired
	private EmployeeMapper employeeMapper;
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	
	//根据用户账号查询用户信息
	public Employee findSysUserByName(String name)throws Exception{
		EmployeeExample sysUserExample = new EmployeeExample();
		EmployeeExample.Criteria criteria = sysUserExample.createCriteria();
		criteria.andNameEqualTo(name);
		
		List<Employee> list = employeeMapper.selectByExample(sysUserExample);
		
		if(list!=null && list.size()==1){
			return list.get(0);
		}	
		
		return null;
	}

	@Override
	public List<SysPermission> findMenuListByUserId(String username)
			throws Exception {
		
		return sysPermissionMapperCustom.findMenuListByUserId(username);
	}

	@Override
	public List<SysPermission> findPermissionListByUserId(String username)
			throws Exception {
		
		return sysPermissionMapperCustom.findPermissionListByUserId(username);
	}

	//根据用户帐号，查询所有角色和其权限列表
	@Override
	public SysRole findRolesAndPermissionsByUserId(String userId) {
		return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userId);
	}

	@Override
	public List<SysPermission> findAllMenus() {
		SysPermissionExample example = new SysPermissionExample();
		SysPermissionExample.Criteria criteria = example.createCriteria();
		//criteria.andTypeLike("%menu%");
		criteria.andTypeEqualTo("menu");
		return sysPermissionMapper.selectByExample(example);
	}

	//查询所有角色和其权限列表
		@Override
	public List<SysRole> findRolesAndPermissions() {
		return sysPermissionMapperCustom.findRoleAndPermissionList();
	}

	@Override
	public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
		//添加角色
		roleMapper.insert(role);
		//添加角色和权限关系表
		for (int i = 0; i < permissionIds.length; i++) {
			SysRolePermission rolePermission = new SysRolePermission();
			//16进制随机码
			String uuid = UUID.randomUUID().toString();
			rolePermission.setId(uuid);
			rolePermission.setSysRoleId(role.getId());
			rolePermission.setSysPermissionId(permissionIds[i]+"");
			rolePermissionMapper.insert(rolePermission);
		}
			
	}
	
	@Override
	public void addSysPermission(SysPermission permission) {
		sysPermissionMapper.insert(permission);
	}
	
	@Override
	public List<MenuTree> getAllMenuAndPermision() {
		return sysPermissionMapperCustom.getAllMenuAndPermision();
	}
	
	@Override
	public List<SysPermission> findPermissionsByRoleId(String roleId) {
		return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
	}
	
	@Override
	public void updateRoleAndPermissions(String roleId, int[] permissionIds) {
		//先删除角色权限关系表中角色的权限关系
		SysRolePermissionExample example = new SysRolePermissionExample();
		SysRolePermissionExample.Criteria criteria = example.createCriteria();
		criteria.andSysRoleIdEqualTo(roleId);
		rolePermissionMapper.deleteByExample(example);
		//重新创建角色权限关系
		for (Integer pid : permissionIds) {
			SysRolePermission rolePermission = new SysRolePermission();
			String uuid = UUID.randomUUID().toString();
			rolePermission.setId(uuid);
			rolePermission.setSysRoleId(roleId);
			rolePermission.setSysPermissionId(pid.toString());
			
			rolePermissionMapper.insert(rolePermission);
		}
	}
}
