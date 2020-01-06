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
 * activiti�ṩ����Ķ���
 *   1.getRepositoryService() ��ȡRepositoryService,�������̶����ҵ��
 *   2.getRuntimeService()    ��ȡRuntimeService����������ʵ����ص�ҵ��
 *   3.getTaskService()       ��ȡTaskService�����������ҵ��
 *     �������ݿ��
 *     --act_re_deployment  ���̲�������
 *     --act_re_procdef     ���̶����  Id: helloProcess:1:4 key:�汾�ţ������
 *     --act_ge_bytearray   ������Դ��  �洢����Դ�ļ���bpnm/png
 *     --act_ru_task        ��ǰ���̻�������
 *     --act_ru_execution   ����ִ�б�
 *     --act_hi_procinst    ����ʵ������ʷ����
 *     ���̶��������ʵ����
 *            ���̶��壬��һ������ģ�壨java�ࣩ������ʵ���Ǿ���ĳһ�����̲�����java����
 * @author Allen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"classpath:spring/applicationContext.xml","classpath:spring/springmvc.xml"})
public class TestBaoxiaoBill {
	//��spring�ķ�ʽ��ȡ�������棬Ĭ�϶�ȡactiviti.cfg.xml
	//ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	
	
	@Test //1.�������̶���
	public void testDeployDB1() throws FileNotFoundException {
		InputStream input = new FileInputStream("D:\\diagram\\baoxiaoProcess.zip");
		ZipInputStream zipInput = new ZipInputStream(input );
		Deployment deployment = repositoryService .createDeployment()
									.name("��������")
									.addZipInputStream(zipInput)
									.deploy();
		System.out.println(deployment.getId());
		System.out.println(deployment.getName());
	}
	
	@Test//2.��������ʵ��
	public void testStartProcess() {
		//ʹ�õ�ǰ�����ȡ�����̶����key����������ƾ������̶����key��
		//String key= Constants.BAOXIAO_KEY;
		String baoxiaoId = "baoxiaoProcess";
		
		Map<String, Object> variables = new HashMap<String,Object>();
		variables.put("inputUser", "zhang");//��ʾΩһ�û�

		//��ʽ��baoxiao.id����ʽ��ʹ�����̱�����
		//String objId = key+"."+baoxiaoId;
		//variables.put("objId", objId);

		ProcessInstance pi = runtimeService.startProcessInstanceByKey(baoxiaoId, variables);
		System.out.println("����ʵ��ID:" + pi.getId());
		System.out.println("���̶���ID:" + pi.getProcessDefinitionId());		
	}
	
	@Test //3.���ݴ����˲�ѯ��������
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
	
	@Test //5.�������̱���
	public void testSetVariable() {
		//processEngine.getTaskService().setVariable(arg0, arg1, arg2);
		//processEngine.getRuntimeService().setVariable(arg0, arg1, arg2);
		//processEngine.getTaskService().complete(arg0, arg1); ��֧����
		//processEngine.getRuntimeService().startProcessInstanceByKey(arg0, arg1);�������̴�����
		
		String taskId = "5702";
		//�������̱���
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("message", "���С�ڵ���5000");
		//map.put("money", 5000D);
		taskService.setVariables(taskId , map);
		System.out.println("���̱����������");
	}
	
	@Test //6.�õ����̱���
	public void testGetVariable() {
		String taskId = "702";
		String remark = (String) taskService.getVariable(taskId, "remark");
		Double money = (Double) taskService.getVariable(taskId, "money");
		System.out.println(remark);
		System.out.println(money);
	}
	
	@Test //4. �����ƽ���������
	public void testFinishTask(){
		String taskId = "5902";
//		Map<String,Object> map = new HashMap<String,Object>();
//		map.put("message", "��ͬ��");
		taskService.complete(taskId);;
		System.out.println("���̽���");
	}
	
	@Test
	public void testFindOutComeListByTaskId() {
		//���ش�����ߵ����Ƽ���
		List<String> list = new ArrayList<String>();
		String taskId = "903";
		//1:ʹ������ID����ѯ�������
		Task task = taskService.createTaskQuery().taskId(taskId ).singleResult();
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
		
		for (String name : list) {
			System.out.println(name);
		}
		
	}
	
	@Test //5. ɾ�����̶��壬�������̲��������ɾ��
	public void testRemovePD() {
		String deploymentID ="";
		repositoryService.deleteDeployment(deploymentID );
		//ps.ǿ��ɾ�������̶������л������ʵ������ֱ��ȫ��ɾ��
		repositoryService.deleteDeployment(deploymentID,true);
		System.out.println("ɾ���ɹ�");
	}
	
	@Test //6.�鿴���̶���ͼ
	public void testViewPic() throws IOException {
		String deploymentId = "1";
		String resourceName = "diagram/HelloProcess.png";
		InputStream is = repositoryService.getResourceAsStream(deploymentId, resourceName);
		File targetFile = new File("d:/"+resourceName);
		//FileUtils.copyInputStreamToFile(is, targetFile );
		System.out.println("�������");
	
	}
	
	@Test //7.�ж�����ʵ���Ƿ����
	public void testProcessInstanceExist() {
		String processId = "101";
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()
		                       .processInstanceId(processId).singleResult();
		if(pi!=null) {
			System.out.println("�����������С�����");
		}else {
			System.out.println("���̽�����");
		}
	}
	
	
	
}
