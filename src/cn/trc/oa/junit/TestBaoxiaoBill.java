package cn.trc.oa.junit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;


import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.trc.oa.utils.Constants;


/**
 * activiti提供服务的对象：
 *   1.getRepositoryService() 获取RepositoryService,处理流程定义的业务
 *   2.getRuntimeService()    获取RuntimeService，处理流程实例相关的业务
 *   3.getTaskService()       获取TaskService，处理任务的业务
 *     流程数据库表：
 *     --act_re_deployment  流程部署对象表
 *     --act_re_procdef     流程定义表  Id: helloProcess:1:4 key:版本号：随机数
 *     --act_ge_bytearray   流程资源表  存储了资源文件：bpnm/png
 *     --act_ru_task        当前流程活动的任务表
 *     --act_ru_execution   任务执行表
 *     --act_hi_procinst    流程实例（历史）表
 *     流程定义和流程实例：
 *            流程定义，是一个流程模板（java类），流程实例是具体某一个流程操作（java对象）
 * @author Allen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:spring/applicationContext.xml","classpath:spring/springmvc.xml"})
public class TestBaoxiaoBill {
	//以spring的方式获取流程引擎，默认读取activiti.cfg.xml
	//ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	
	
	@Test //1.部署流程定义
	public void testDeployDB1() throws FileNotFoundException {
		InputStream input = new FileInputStream("D:\\diagram\\baoxiaoProcess.zip");
		ZipInputStream zipInput = new ZipInputStream(input );
		Deployment deployment = repositoryService .createDeployment()
									.name("报销测试")
									.addZipInputStream(zipInput)
									.deploy();
		System.out.println(deployment.getId());
		System.out.println(deployment.getName());
	}
	
	@Test//2.启动流程实例
	public void testStartProcess() {
		//使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
		//String key= Constants.BAOXIAO_KEY;
		String baoxiaoId = "baoxiaoProcess";
		
		Map<String, Object> variables = new HashMap<String,Object>();
		variables.put("inputUser", "zhang");//表示惟一用户

		//格式：baoxiao.id的形式（使用流程变量）
		//String objId = key+"."+baoxiaoId;
		//variables.put("objId", objId);

		ProcessInstance pi = runtimeService.startProcessInstanceByKey(baoxiaoId, variables);
		System.out.println("流程实例ID:" + pi.getId());
		System.out.println("流程定义ID:" + pi.getProcessDefinitionId());		
	}
	
	@Test //3.根据待办人查询待办事务
	public void testFindTaskByAssignee() {
		String assignee = "mike";
		List<Task> list = taskService.createTaskQuery()
								.taskAssignee(assignee)
								.list();
		for (Task task : list) {
			System.out.println(task.getId());
			System.out.println(task.getAssignee());
			System.out.println(task.getProcessInstanceId());
			System.out.println(task.getProcessDefinitionId());
		}
	}
	
	@Test //5.设置流程变量
	public void testSetVariable() {
		//processEngine.getTaskService().setVariable(arg0, arg1, arg2);
		//processEngine.getRuntimeService().setVariable(arg0, arg1, arg2);
		//processEngine.getTaskService().complete(arg0, arg1); 分支连线
		//processEngine.getRuntimeService().startProcessInstanceByKey(arg0, arg1);分配流程代办人
		
		String taskId = "5702";
		//保存流程变量
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("message", "金额小于等于5000");
		//map.put("money", 5000D);
		taskService.setVariables(taskId , map);
		System.out.println("流程变量设置完成");
	}
	
	@Test //6.得到流程变量
	public void testGetVariable() {
		String taskId = "702";
		String remark = (String) taskService.getVariable(taskId, "remark");
		Double money = (Double) taskService.getVariable(taskId, "money");
		System.out.println(remark);
		System.out.println(money);
	}
	
	@Test //4. 流程推进（结束）
	public void testFinishTask(){
		String taskId = "5902";
//		Map<String,Object> map = new HashMap<String,Object>();
//		map.put("message", "不同意");
		taskService.complete(taskId);;
		System.out.println("流程结束");
	}
	
	@Test
	public void testFindOutComeListByTaskId() {
		//返回存放连线的名称集合
		List<String> list = new ArrayList<String>();
		String taskId = "903";
		//1:使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery().taskId(taskId ).singleResult();
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
		
		for (String name : list) {
			System.out.println(name);
		}
		
	}
	
	@Test //5. 删除流程定义，根据流程部署对象来删除
	public void testRemovePD() {
		String deploymentID ="";
		repositoryService.deleteDeployment(deploymentID );
		//ps.强行删除，流程定义中有活动的流程实例可以直接全部删除
		repositoryService.deleteDeployment(deploymentID,true);
		System.out.println("删除成功");
	}
	
	@Test //6.查看流程定义图
	public void testViewPic() throws IOException {
		String deploymentId = "1";
		String resourceName = "diagram/HelloProcess.png";
		InputStream is = repositoryService.getResourceAsStream(deploymentId, resourceName);
		File targetFile = new File("d:/"+resourceName);
		//FileUtils.copyInputStreamToFile(is, targetFile );
		System.out.println("操作完成");
	
	}
	
	@Test //7.判断流程实例是否结束
	public void testProcessInstanceExist() {
		String processId = "101";
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
		                       .processInstanceId(processId).singleResult();
		if(pi!=null) {
			System.out.println("流程正在运行。。。");
		}else {
			System.out.println("流程结束！");
		}
	}
	
	
	
}
