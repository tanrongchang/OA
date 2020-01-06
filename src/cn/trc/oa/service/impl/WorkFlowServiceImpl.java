package cn.trc.oa.service.impl;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.trc.oa.mapper.BaoxiaobillMapper;
import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Baoxiaobill;
import cn.trc.oa.pojo.Employee;
import cn.trc.oa.service.WorkFlowService;
import cn.trc.oa.utils.Constants;



@Service
public class WorkFlowServiceImpl implements WorkFlowService {
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private BaoxiaobillMapper baoxiaoBillMapper;
	
	@Override
	public List<ProcessDefinition> findAllProcessDefinitions() {
		return repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().desc().list();
	}

	@Override
	public List<Deployment> findAllDeployments() {
		return repositoryService.createDeploymentQuery().list();
	}

	@Override
	public void savedeployProcess(String processName,InputStream is) {
		ZipInputStream zipInput = new ZipInputStream(is);
		repositoryService.createDeployment().name(processName)
						 .addZipInputStream(zipInput)
						 .deploy();
		
	}

	@Override
	public InputStream findImageInputStream(String deploymentId, String imageName) {
		return repositoryService.getResourceAsStream(deploymentId, imageName);
	}

	@Override
	public void saveStartProcess(Baoxiaobill baoxiaoBill, ActiveUser activeUser) {
		//1.保存请假单
		baoxiaoBill.setCreatdate(new Date());
		baoxiaoBill.setState(1);
		baoxiaoBill.setUserId(activeUser.getUserid());
		baoxiaoBillMapper.insert(baoxiaoBill);//mybatis把主键回填到pojo对象
		
		//2.启动流程
		String key = Constants.BAOXIAO_KEY;
		Map<String,Object> map = new HashMap<String,Object>();
		//设置流程变量=代办人
		map.put("inputUser", activeUser.getUsername());
		String BUSSINESS_KEY = Constants.BAOXIAO_KEY+"."+baoxiaoBill.getId();
		System.out.println(BUSSINESS_KEY);
		//runtimeService.startProcessInstanceByKey(key, map);
		//怎样把流程业务数据和应用的业务表的数据相关联:例如：如果得到流程实例，可以查询出对应的员工信息
		runtimeService.startProcessInstanceByKey(key, BUSSINESS_KEY, map);
		
	}

	@Override
	public List<Task> findTaskListByUserId(String name) {
		return taskService.createTaskQuery().taskAssignee(name).list();
	}

	@Override
	public Baoxiaobill findBillByTask(String taskId) {
		//1.根据任务id查找流程实例
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
								.processInstanceId(task.getProcessInstanceId()).singleResult();
		String bussiness_key = pi.getBusinessKey();//leaveProcess.23
		System.out.println(bussiness_key);
		String id = bussiness_key.split("\\.")[1];
		System.out.println(id);
		Baoxiaobill bill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(id));
		return bill;
	}

	@Override
	public List<Comment> findCommentList(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		List<Comment> list = taskService.getProcessInstanceComments(task.getProcessInstanceId());
		return list;
	}

	@Override
	public void saveSubmitTask(long id, String taskId, String comment,String outcome,String name) {
		// 1. 添加批注 
		//使用任务ID，查询任务对象，获取流程流程实例ID
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//加当前任务的审核人
		Authentication.setAuthenticatedUserId(name);
		//获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();

		this.taskService.addComment(taskId, processInstanceId, comment);
		
		//2.流程推进
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("message", outcome);
		taskService.setVariables(taskId, map);
		taskService.complete(taskId);
		
		//3.如果当前流程结束，修改业务表状态
		ProcessInstance pi = this.runtimeService.createProcessInstanceQuery()
									.processInstanceId(processInstanceId).singleResult();
		if (pi == null) { //流程结束
			Baoxiaobill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
			bill.setState(2);  
			baoxiaoBillMapper.updateByPrimaryKey(bill);
		}
		
	}

	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		//使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//查询流程定义的对象
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
						.processDefinitionId(processDefinitionId)//使用流程定义ID查询
						.singleResult();
		return pd;
	}
	
	@Override
	public void deleteDeployment(String deploymentId) {
		repositoryService.deleteDeployment(deploymentId);
	}

	@Override
	public Map<String, Object> findCoordingByTask(String taskId) {
		//存放坐标
		Map<String, Object> map = new HashMap<String,Object>();
		//使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//使用任务ID查询
					.singleResult();
		//获取流程定义的ID
		String processDefinitionId = task.getProcessDefinitionId();
		//获取流程定义的实体对象（对应.bpmn文件中的数据）
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
											.processInstanceId(processInstanceId)//使用流程实例ID查询
											.singleResult();
		//获取当前活动的ID
		String activityId = pi.getActivityId();
		//获取当前活动对象
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
		//获取坐标
		map.put("x", activityImpl.getX());
		map.put("y", activityImpl.getY());
		map.put("width", activityImpl.getWidth()); 
		map.put("height", activityImpl.getHeight());
		return map;
	}

	@Override
	public List<String> findOutComeListByTaskId(String taskId) {
		//返回存放连线的名称集合
		List<String> list = new ArrayList<String>();
		//1:使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//2：获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//3：查询ProcessDefinitionEntiy对象
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
		//使用任务对象Task获取流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
					.processInstanceId(processInstanceId)//使用流程实例ID查询
					.singleResult();
		//获取当前活动的id
		String activityId = pi.getActivityId();
		//4：获取当前的活动
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
		//5：获取当前活动完成之后连线的名称
		List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
		if(pvmList!=null && pvmList.size()>0){
			for(PvmTransition pvm:pvmList){
				String name = (String) pvm.getProperty("name");
				if(StringUtils.isNotBlank(name)){
					list.add(name);
				} else{
					list.add("默认提交");
				}
			}
		}
		return list;
	}

	//根据业务表的数据关联流程任务
	@Override
	public Task findTaskByBussinessKey(String BUSSINESS_KEY) {
		Task task = this.taskService.createTaskQuery().processInstanceBusinessKey(BUSSINESS_KEY).singleResult();
		return task;
	}

	@Override
	public List<Comment> findCommentByBillId(long id) {
		String bussiness_key = Constants.BAOXIAO_KEY+"."+id;
		HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
						.processInstanceBusinessKey(bussiness_key).singleResult();
		List<Comment> commentList = taskService.getProcessInstanceComments(pi.getId());
		return commentList;
	}
	
}
