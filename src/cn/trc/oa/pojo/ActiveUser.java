package cn.trc.oa.pojo;

import java.util.Iterator;
import java.util.List;

import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * 用户身份信息，存入session 由于tomcat将session会序列化在本地硬盘上，所以使用Serializable接口
 * 
 * @author Thinkpad
 * 
 */
public class ActiveUser implements java.io.Serializable {
	private Long userid;//用户id（主键）
	private String username;// 用户名称

	private List<MenuTree> menuTree;// 动态菜单
	private List<SysPermission> perMeuns;//菜单
	private List<SysPermission> permissions;// 权限
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public List<MenuTree> getMenuTree() {
		return menuTree;
	}
	public void setMenuTree(List<MenuTree> menuTree) {
		this.menuTree = menuTree;
	}
	public List<SysPermission> getPerMeuns() {
		return perMeuns;
	}
	public void setPerMeuns(List<SysPermission> perMeuns) {
		this.perMeuns = perMeuns;
	}
	public List<SysPermission> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<SysPermission> permissions) {
		this.permissions = permissions;
	}

	
	
	
}
