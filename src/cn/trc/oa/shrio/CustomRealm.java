package cn.trc.oa.shrio;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import cn.trc.oa.mapper.SysPermissionMapperCustom;
import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.pojo.MenuTree;
import cn.trc.oa.pojo.SysPermission;
import cn.trc.oa.service.SysService;



 

public class CustomRealm extends AuthorizingRealm{
	
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	@Autowired
	private SysService sysService;
	//认证
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

		//1.先认证账号
		//先获取用户的输入 的帐号
		String username = (String) token.getPrincipal();
		//伪代码 
		Employee user = null;
		List<MenuTree> menus = null;
		//List<SysPermission> permenus = null;
		try {
			user = sysService.findSysUserByName(username);
			if(user == null) {
				//帐号不存在 
				return null;  //报异常：  org.apache.shiro.authc.UnknownAccountException
			}
			//查询菜单列表
		menus = sysPermissionMapperCustom.findAllMenus();
		//查询权限菜单列表
		//permenus = sysService.findMenuListByUserId(username);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String password_db = user.getPassword();//密文
		String salt = user.getSalt();
		
		//封装ActiveUser
		ActiveUser activeUser = new ActiveUser();
		activeUser.setMenuTree(menus);
		activeUser.setUserid(user.getId());
		activeUser.setUsername(user.getName());
		//activeUser.setPerMeuns(permenus);
		

		//2.认证密码
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(activeUser,password_db,ByteSource.Util.bytes(salt),"CustomRealm");  //验证密码
		return info;
	}
	
	//授权
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
		ActiveUser user = (ActiveUser) principal.getPrimaryPrincipal();
		
		List<String> permissions = new ArrayList<String>();
		try {
			List<SysPermission> permissionList = sysService.findPermissionListByUserId(user.getUsername());
			
			for (SysPermission permission : permissionList) {
				permissions.add(permission.getPercode());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

		info.addStringPermissions(permissions);
		
		
		return info;
	}
	
}
