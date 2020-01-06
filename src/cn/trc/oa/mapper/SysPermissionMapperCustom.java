package cn.trc.oa.mapper;

import java.util.List;

import cn.trc.oa.pojo.MenuTree;
import cn.trc.oa.pojo.SysPermission;
import cn.trc.oa.pojo.SysRole;



public interface SysPermissionMapperCustom {
	
	//根据用户id查询菜单
	public List<SysPermission> findMenuListByUserId(String username)throws Exception;
	//根据用户id查询权限url
	public List<SysPermission> findPermissionListByUserId(String username)throws Exception;
	
	public List<MenuTree> findAllMenus();
	
	public List<SysPermission> getSubMenu();
	
	public SysRole findRoleAndPermissionListByUserId(String userId);
	
	public List<SysRole> findRoleAndPermissionList();
	
	public List<MenuTree> getAllMenuAndPermision();
	
	public List<SysPermission> findPermissionsByRoleId(String roleId);
	
	
}
