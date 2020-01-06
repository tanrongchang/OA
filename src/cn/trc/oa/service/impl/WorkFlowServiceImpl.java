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
		//1.������ٵ�
		baoxiaoBill.setCreatdate(new Date());
		baoxiaoBill.setState(1);
		baoxiaoBill.setUserId(activeUser.getUserid());
		baoxiaoBillMapper.insert(baoxiaoBill);//mybatis���������pojo����
		
		//2.��������
		String key = Constants.BAOXIAO_KEY;
		Map<String,Object> map = new HashMap<String,Object>();
		//�������̱���=������
		map.put("inputUser", activeUser.getUsername());
		String BUSSINESS_KEY = Constants.BAOXIAO_KEY+"."+baoxiaoBill.getId();
		System.out.println(BUSSINESS_KEY);
		//runtimeService.startProcessInstanceByKey(key, map);
		//����������ҵ�����ݺ�Ӧ�õ�ҵ�������������:���磺����õ�����ʵ�������Բ�ѯ����Ӧ��Ա����Ϣ
		runtimeService.startProcessInstanceByKey(key, BUSSINESS_KEY, map);
		
	}

	@Override
	public List<Task> findTaskListByUserId(String name) {
		return taskService.createTaskQuery().taskAssignee(name).list();
	}

	@Override
	public Baoxiaobill findBillByTask(String taskId) {
		//1.��������id��������ʵ��
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
		// 1. �����ע 
		//ʹ������ID����ѯ������󣬻�ȡ��������ʵ��ID
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//�ӵ�ǰ����������
		Authentication.setAuthenticatedUserId(name);
		//��ȡ����ʵ��ID
		String processInstanceId = task.getProcessInstanceId();

		this.taskService.addComment(taskId, processInstanceId, comment);
		
		//2.�����ƽ�
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("message", outcome);
		taskService.setVariables(taskId, map);
		taskService.complete(taskId);
		
		//3.�����ǰ���̽������޸�ҵ���״̬
		ProcessInstance pi = this.runtimeService.createProcessInstanceQuery()
									.processInstanceId(processInstanceId).singleResult();
		if (pi == null) { //���̽���
			Baoxiaobill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
			bill.setState(2);  
			baoxiaoBillMapper.updateByPrimaryKey(bill);
		}
		
	}

	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		//ʹ������ID����ѯ�������
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//��ȡ���̶���ID
		String processDefinitionId = task.getProcessDefinitionId();
		//��ѯ���̶���Ķ���
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
						.processDefinitionId(processDefinitionId)//ʹ�����̶���ID��ѯ
						.singleResult();
		return pd;
	}
	
	@Override
	public void deleteDeployment(String deploymentId) {
		repositoryService.deleteDeployment(deploymentId);
	}

	@Override
	public Map<String, Object> findCoordingByTask(String taskId) {
		//�������
		Map<String, Object> map = new HashMap<String,Object>();
		//ʹ������ID����ѯ�������
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//ʹ������ID��ѯ
					.singleResult();
		//��ȡ���̶����ID
		String processDefinitionId = task.getProcessDefinitionId();
		//��ȡ���̶����ʵ����󣨶�Ӧ.bpmn�ļ��е����ݣ�
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
		//����ʵ��ID
		String processInstanceId = task.getProcessInstanceId();
		//ʹ������ʵ��ID����ѯ����ִ�е�ִ�ж������ȡ��ǰ���Ӧ������ʵ������
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//��������ʵ����ѯ
											.processInstanceId(processInstanceId)//ʹ������ʵ��ID��ѯ
											.singleResult();
		//��ȡ��ǰ���ID
		String activityId = pi.getActivityId();
		//��ȡ��ǰ�����
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//�ID
		//��ȡ����
		map.put("x", activityImpl.getX());
		map.put("y", activityImpl.getY());
		map.put("width", activityImpl.getWidth()); 
		map.put("height", activityImpl.getHeight());
		return map;
	}

	@Override
	public List<String> findOutComeListByTaskId(String taskId) {
		//���ش�����ߵ����Ƽ���
		List<String> list = new ArrayList<String>();
		//1:ʹ������ID����ѯ�������
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//2����ȡ���̶���ID
		String processDefinitionId = task.getProcessDefinitionId();
		//3����ѯProcessDefinitionEntiy����
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
		//ʹ���������Task��ȡ����ʵ��ID
		String processInstanceId = task.getProcessInstanceId();
		//ʹ������ʵ��ID����ѯ����ִ�е�ִ�ж������������ʵ������
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
					.processInstanceId(processInstanceId)//ʹ������ʵ��ID��ѯ
					.singleResult();
		//��ȡ��ǰ���id
		String activityId = pi.getActivityId();
		//4����ȡ��ǰ�Ļ
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
		//5����ȡ��ǰ����֮�����ߵ�����
		List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
		if(pvmList!=null && pvmList.size()>0){
			for(PvmTransition pvm:pvmList){
				String name = (String) pvm.getProperty("name");
				if(StringUtils.isNotBlank(name)){
					list.add(name);
				} else{
					list.add("Ĭ���ύ");
				}
			}
		}
		return list;
	}

	//����ҵ�������ݹ�����������
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
