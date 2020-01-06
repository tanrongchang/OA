package cn.trc.oa.junit;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.trc.oa.mapper.SysPermissionMapperCustom;
import cn.trc.oa.pojo.MenuTree;
import cn.trc.oa.pojo.SysPermission;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:spring/applicationContext.xml","classpath:spring/springmvc.xml"})
public class TestTreeMenu {
	
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	
	@Test
	public void testMenu() { 
		List<MenuTree> list = sysPermissionMapperCustom.findAllMenus();
		for (MenuTree menu : list) {
			System.out.println(menu.getId()+"::"+menu.getName());
			for (SysPermission submenu : menu.getChildren()) {
				System.out.println("\t"+submenu.getName()+","+submenu.getUrl()+","+submenu.getPercode());
			}
		}
	
	
	}
}
