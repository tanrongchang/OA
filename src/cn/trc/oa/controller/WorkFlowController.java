package cn.trc.oa.controller;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Baoxiaobill;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.service.WorkFlowService;
import cn.trc.oa.utils.Constants;




@Controller
public class WorkFlowController {
	
	@Autowired
	private WorkFlowService workFlowService;
	
	//ok
	@RequestMapping("/submitTask")
	public String submitTask(long id,String taskId,String comment,String outcome,HttpSession session) {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		workFlowService.saveSubmitTask(id,taskId,comment,outcome,activeUser.getUsername());
		return "redirect:/myTaskList";
		
	}
	
	//ok
	@RequestMapping("/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId) {
		//从流程数据查找业务数据
		System.out.println(taskId);
		Baoxiaobill bill = workFlowService.findBillByTask(taskId);
		//查询批注信息
		List<Comment> commentList = workFlowService.findCommentList(taskId);
		//查询连线分支信息
		List<String> outComeList = workFlowService.findOutComeListByTaskId(taskId);
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("bill", bill);
		mv.addObject("commentList", commentList);
		mv.addObject("outComeList", outComeList);
		mv.addObject("taskId", taskId);
		mv.setViewName("approve_baoxiao");
		return mv;
		
	}
	
	//ok
	@RequestMapping("/saveStartLeave")
	public String saveStartLeave(Baoxiaobill baoxiaoBill,HttpSession session) {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		workFlowService.saveStartProcess(baoxiaoBill,activeUser);
		return "redirect:/myTaskList";
		
	}
	
	//ok
	@RequestMapping("/myTaskList")
	public ModelAndView myTaskList(HttpServletRequest req) {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<Task> list = workFlowService.findTaskListByUserId(activeUser.getUsername());
		ModelAndView mv = new ModelAndView();
		mv.addObject("taskList",list);
		mv.setViewName("workflow_task");
		return mv;
		
	}
	
	//ok
	@RequestMapping("/deployProcess")
	public String deployProcess(String processName,MultipartFile fileName) throws IOException {
		workFlowService.savedeployProcess(processName, fileName.getInputStream());
		return "redirect:/processDefinitionList";
		
	}
	
	//ok
	@RequestMapping("/processDefinitionList")
	public ModelAndView processDefinitionList() {
		List<ProcessDefinition> pdList = workFlowService.findAllProcessDefinitions();
		List<Deployment> deployList = workFlowService.findAllDeployments();
		
		ModelAndView mv = new ModelAndView();
		mv.addObject("pdList",pdList);
		mv.addObject("depList",deployList);
		mv.setViewName("workflow_list");
		return mv;
		
	}
	
	//ok
	@RequestMapping("/delDeployment/{deploymentId}")
	public String delDeployment(@PathVariable String deploymentId) {
		workFlowService.deleteDeployment(deploymentId);
		return "redirect:/processDefinitionList";
		
	}
	
	//ok
	@RequestMapping("/viewImage")
	public String viewImage(String deploymentId,String imageName,HttpServletResponse response) throws IOException{

		//2：获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
		InputStream in = workFlowService.findImageInputStream(deploymentId,imageName);
		//3：从response对象获取输出流
		OutputStream out = response.getOutputStream();
		//4：将输入流中的数据读取出来，写到输出流中
		for(int b=-1;(b=in.read())!=-1;){
			out.write(b);
		}
		out.close();
		in.close();
		return null;
	}
	
	//ok
	@RequestMapping("/viewCurrentImage")
	public ModelAndView viewCurrentImage(String taskId){
		/**一：查看流程图*/
		//1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
		ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(taskId);
		ModelAndView mv = new ModelAndView();
		mv.addObject("deploymentId", pd.getDeploymentId());
		mv.addObject("imageName", pd.getDiagramResourceName());
		/**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
		Map<String, Object> map = workFlowService.findCoordingByTask(taskId);

		mv.addObject("acs", map);
		mv.setViewName("viewimage");
		return mv;
	}
	
	
}