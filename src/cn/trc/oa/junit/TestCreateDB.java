package cn.trc.oa.junit;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

public class TestCreateDB {
	
	@Test
	public void testSpring() {
		ProcessEngineConfiguration config = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("spring/applicationContext-activiti.xml");
		ProcessEngine processEngine = config.buildProcessEngine();
		System.out.println(processEngine);
	}
}
