package cn.trc.oa.pojo;

import java.util.List;

/**
 * 菜单类
 * @author Allen
 *
 */
public class MenuTree {
	
	//定义一级菜单的信息
	private int id;
	private String name;
	
	private List<SysPermission> children;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SysPermission> getChildren() {
		return children;
	}

	public void setChildren(List<SysPermission> children) {
		this.children = children;
	}
	
	
}
