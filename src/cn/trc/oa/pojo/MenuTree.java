package cn.trc.oa.pojo;

import java.util.List;

/**
 * �˵���
 * @author Allen
 *
 */
public class MenuTree {
	
	//����һ���˵�����Ϣ
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
