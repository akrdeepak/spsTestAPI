package com.capgemini;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpsApiMainApplicationTests {

	@Test
	public void contextLoads() {
		SpsApiMainApplication mainApp = new SpsApiMainApplication();
		assertNotNull(mainApp);
	}

}
