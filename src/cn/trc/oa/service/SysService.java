package cn.trc.oa.service;

import java.util.List;

import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.pojo.MenuTree;
import cn.trc.oa.pojo.SysPermission;
import cn.trc.oa.pojo.SysRole;




public interface SysService {
	
	//根据用户账号查询用户信息
	public Employee findSysUserByName(String name)throws Exception;
	
	//根据用户id查询权限范围的菜单
	public List<SysPermission> findMenuListByUserId(String username) throws Exception;
	
	//根据用户id查询权限范围的url
	public List<SysPermission> findPermissionListByUserId(String username) throws Exception;
	
	//根据用户id查询权限
	public SysRole findRolesAndPermissionsByUserId(String userName);
	
	//查询所有menu类permission
	public List<SysPermission> findAllMenus();
	
	public List<SysRole> findRolesAndPermissions();

	public void addRoleAndPermissions(SysRole role, int[] permissionIds);

	public void addSysPermission(SysPermission permission);

	public List<MenuTree> getAllMenuAndPermision();

	public List<SysPermission> findPermissionsByRoleId(String roleId);

	public void updateRoleAndPermissions(String roleId, int[] permissionIds);
}
