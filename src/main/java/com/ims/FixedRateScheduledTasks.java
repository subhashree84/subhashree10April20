package com.ims;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ims.service.PoolService;

@EnableScheduling
@Configuration
@ConditionalOnProperty(name = "spring.enable.fixedrate.scheduling")
public class FixedRateScheduledTasks {

	@Autowired
	PoolService poolService;
	
	@Value("${app.mapit.eventPoolReqUrl}")
	private String eventPoolReqUrl;

	@Value("${app.mapit.candidatePoolReqUri}")
	private String candidatePoolReqUri;
	
	@Value("${app.mapit.processPoolReqUrl}")
	private String processPoolReqUrl;
	
	@Scheduled(fixedRateString = "${spring.fixedrate.schedule.string}")
	public void scheduleTaskWithFixedRate() throws InterruptedException {
	    task1();
	    task2();
	    //task3();
	}

	public void task1() throws InterruptedException {
		poolService.poolEvent(eventPoolReqUrl);
	}

	public void task2() throws InterruptedException {
		poolService.poolAllCandidates(candidatePoolReqUri);
	}
	
	public void task3() throws InterruptedException {
		poolService.poolAllProcesses(processPoolReqUrl);
	}
}
