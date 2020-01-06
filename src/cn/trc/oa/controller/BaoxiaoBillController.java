package cn.trc.oa.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Baoxiaobill;
import cn.trc.oa.service.BaoxiaoBillService;
import cn.trc.oa.service.WorkFlowService;
import cn.trc.oa.utils.Constants;

@Controller
public class BaoxiaoBillController {
	
	@Autowired
	private BaoxiaoBillService baoxiaoService;
	
	@Autowired
	private WorkFlowService workFlowService;
	
	private static final int PAGE_SIZE = 10;
	
	//我的报销单
	@RequestMapping("/myBaoxiaoBill")
	public String myBaoxiaoBill(ModelMap model,HttpSession session,
			@RequestParam(value="pageNum",required=false,defaultValue="1") int pageNum) {
		//1.查询所有的报销单信息，返回List
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		PageHelper.startPage(pageNum,PAGE_SIZE);
		List<Baoxiaobill> list = baoxiaoService.findBaoxiaoBillListByUserId(activeUser.getUserid());
		PageInfo page = new PageInfo<>(list);
		model.addAttribute("baoxiaoList", list);
		model.addAttribute("page", page);
		return "baoxiao_list";
		
	}
	//删除报销单记录
	@RequestMapping("/delBill")
	public String delBill(long billId){
		baoxiaoService.deleteBillById(billId);
		return "redirect:/myBaoxiaoBill";
		
	}
	
	//审核任务信息
	@RequestMapping("/viewHisComment")
	public String viewTaskForm(long billId,ModelMap model) {
		//从流程数据查找业务数据
		Baoxiaobill bill = baoxiaoService.findBillById(billId);
		//查询批注信息
		List<Comment> commentList = workFlowService.findCommentByBillId(billId);
		
		model.addAttribute("bill", bill);
		model.addAttribute("commentList", commentList);
		return "viewtaskform";
		
	}
	
	//报销流程图
	@RequestMapping("/viewCurrentImageByBill")
	public ModelAndView viewCurrentImage(long billId,ModelMap model){
		String BUSSINESS_KEY = Constants.BAOXIAO_KEY+"."+billId;
		System.out.println(BUSSINESS_KEY);
		Task task = workFlowService.findTaskByBussinessKey(BUSSINESS_KEY);
		System.out.println(task.getId());
		/**一：查看流程图*/
		//1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
		ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(task.getId());
		ModelAndView mv = new ModelAndView();
		mv.addObject("deploymentId", pd.getDeploymentId());
		mv.addObject("imageName", pd.getDiagramResourceName());
		/**二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中*/
		Map<String, Object> map = workFlowService.findCoordingByTask(task.getId());

		mv.addObject("acs", map);
		mv.setViewName("viewimage");
		return mv;
	}
	
}
