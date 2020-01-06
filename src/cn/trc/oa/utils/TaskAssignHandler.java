package cn.trc.oa.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.shiro.SecurityUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.service.EmployeeService;



public class TaskAssignHandler implements TaskListener {

	@Override
	public void notify(DelegateTask task) {
		
	    //spring容器
		WebApplicationContext context =ContextLoader.getCurrentWebApplicationContext();
		EmployeeService employeeService = (EmployeeService) context.getBean("employeeService");
		
//		//Servlet API
//		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		HttpSession session = servletRequestAttributes.getRequest().getSession();
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		Employee employee = employeeService.findEmpById(activeUser.getUserid());
//		
		//根据当前登陆员工获取主管信息
		Employee manager = employeeService.findEmpById(employee.getManagerId());
//						
		task.setAssignee(manager.getName());
	}

}
