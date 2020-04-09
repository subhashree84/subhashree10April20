package com.ims.service;

public interface PoolService {

	void poolEvent(String eventPoolReqUrl);
	void poolAllCandidates(String candidatePoolReqUrl);
	void poolCandidateByEventId(String candidatePoolReqUrl, String eventId);
	void poolAllProcesses(String processPoolReqUrl);
}
