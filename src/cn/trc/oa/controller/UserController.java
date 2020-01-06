package cn.trc.oa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import cn.trc.oa.exception.CustomException;
import cn.trc.oa.mapper.EmployeeCustomMapper;
import cn.trc.oa.mapper.SysPermissionMapperCustom;
import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.pojo.EmployeeCustom;
import cn.trc.oa.pojo.MenuTree;
import cn.trc.oa.pojo.SysPermission;
import cn.trc.oa.pojo.SysRole;
import cn.trc.oa.service.EmployeeService;
import cn.trc.oa.service.SysService;
import cn.trc.oa.utils.Constants;




@Controller
public class UserController {
	
	@Autowired
	private SysService sysService;
	@Autowired
	private EmployeeService empService;
	@Autowired
	private EmployeeCustomMapper employeeCustomMapper;
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	
	@RequestMapping("/login")//不是真正登陆业务逻辑 ，登陆失败返回的处理代码
	public String login(HttpServletRequest request,Model model)throws Exception{
		//从作用域中提取错误提示
		
		String exceptionError = (String) request.getAttribute("shiroLoginFailure");
		if(exceptionError!=null) {
			if(UnknownAccountException.class.getName().equals(exceptionError)||
					IncorrectCredentialsException.class.getName().equals(exceptionError)) {
				model.addAttribute("error", "帐号或密码错误");
			}else {
				model.addAttribute("error", "帐号或密码不能为空");
			}
			
//			if("validateCodeError".equals(exceptionError)) {
//				throw new CustomException("验证码错误");
//			}
		}
		//转发回login.jsp FormAuthenticationFilter		
		return "login";
		
		
	}
	//系统首页
	@RequestMapping("/main")
	public String first(Model model)throws Exception{
			
		//从shiro的session中取activeUser
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		//通过model传到页面
		model.addAttribute("activeUser", activeUser);
		return "index";
	}
	
	@RequestMapping("/findUserList")
	public String findUserList(Model model)throws Exception{
		List<EmployeeCustom> emp = employeeCustomMapper.findAllUser();
		List<SysRole> role = empService.findAllRole();
		model.addAttribute("userList",emp);
		model.addAttribute("allRoles",role);
		return "userlist";
	}
	
	@RequestMapping("/assignRole")
	@ResponseBody
	public Map<String, String> assignRole(String roleId,String userId) {
		Map<String, String> map = new HashMap<>(); 
		System.out.println(roleId+"======="+userId);
		try {
			empService.updateEmployeeRole(roleId, userId);
			map.put("msg", "权限分配成功");
		} catch (Exception e) {
			e.printStackTrace();
			map.put("msg", "权限分配失败");
		}
		return map;
	}
	
	@RequestMapping("/findNextManager")
	@ResponseBody
	public List<Employee> findNextManager(int managerId) {
		managerId++;
		List<Employee> list = empService.findEmployeeByManagerId(managerId);
		return list;
		
	}

	@RequestMapping("/saveUser")
	public String saveUser(Employee user) {
		System.out.println(user.getName());
		empService.saveUser(user);
		return "redirect:/findUserList";		
	}
	
	@RequestMapping("/viewPermissionByUser")
	@ResponseBody
	public SysRole viewPermissionByUser(String userName) {
		SysRole sysRole = sysService.findRolesAndPermissionsByUserId(userName);

		System.out.println(sysRole.getName()+"," +sysRole.getPermissionList());
		return sysRole;
	}
	
	
	@RequestMapping("/toAddRole")
	public ModelAndView toAddRole() {
		List<MenuTree> allPermissions = sysPermissionMapperCustom.findAllMenus();
		List<SysPermission> menus = sysService.findAllMenus();
		List<SysRole> permissionList = sysService.findRolesAndPermissions();
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("allPermissions", allPermissions);
		mv.addObject("menuTypes", menus);
		mv.addObject("roleAndPermissionsList", permissionList);
		mv.setViewName("rolelist");
		
		return mv;
	}
	
	@RequestMapping("/saveRoleAndPermissions")
	public String saveRoleAndPermissions(SysRole role,int[] permissionIds) {
		//设置role主键，使用uuid
		String uuid = UUID.randomUUID().toString();
		role.setId(uuid);
		//默认可用
		role.setAvailable("1");
		sysService.addRoleAndPermissions(role, permissionIds);
		return "redirect:/toAddRole";
	}
	
	@RequestMapping("/saveSubmitPermission")
	public String saveSubmitPermission(SysPermission permission) {
		if (permission.getAvailable() == null) {
			permission.setAvailable("0");
		}
		sysService.addSysPermission(permission);
		return "redirect:/toAddRole";
	}
	
	
	@RequestMapping("/findRoles")  //rest
	public ModelAndView findRoles() {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<SysRole> roles = empService.findAllRole();
		List<MenuTree> allMenuAndPermissions = sysService.getAllMenuAndPermision();
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("allRoles", roles);
		mv.addObject("activeUser",activeUser);
		mv.addObject("allMenuAndPermissions", allMenuAndPermissions);
		
		mv.setViewName("permissionlist");
		return mv;
	}
	
	@RequestMapping("/loadMyPermissions")
	@ResponseBody
	public List<SysPermission> loadMyPermissions(String roleId) {
		List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);
		
		for (SysPermission sysPermission : list) {
			System.out.println(sysPermission.getId()+","+sysPermission.getType()+"\n"+sysPermission.getName() + "," + sysPermission.getUrl()+","+sysPermission.getPercode());
		}
		return list;
	}
	
	@RequestMapping("/updateRoleAndPermission")
	public String updateRoleAndPermission(String roleId,int[] permissionIds) {
		sysService.updateRoleAndPermissions(roleId, permissionIds);
		return "redirect:/findRoles";		
	}
	
	


	

}
