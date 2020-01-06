package cn.trc.oa.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import cn.trc.oa.pojo.ActiveUser;
import cn.trc.oa.pojo.Baoxiaobill;
import cn.trc.oa.pojo.Employee;



public interface WorkFlowService {
	
	public List<ProcessDefinition>  findAllProcessDefinitions();
	
	public List<Deployment> findAllDeployments(); 
	
	public void savedeployProcess(String processName,InputStream is);

	public InputStream findImageInputStream(String deploymentId, String imageName);

	public void saveStartProcess(Baoxiaobill baoxiaoBill, ActiveUser activeUser);

	public List<Task> findTaskListByUserId(String name);

	public Baoxiaobill findBillByTask(String taskId);

	public List<Comment> findCommentList(String taskId);

	public void saveSubmitTask(long id, String taskId, String comment,String outcome,String name);

	public ProcessDefinition findProcessDefinitionByTaskId(String taskId);

	public Map<String, Object> findCoordingByTask(String taskId);

	public void deleteDeployment(String deploymentId);
	
	public List<String> findOutComeListByTaskId(String taskId);

	public Task findTaskByBussinessKey(String BUSSINESS_KEY);

	public List<Comment> findCommentByBillId(long id);
}
