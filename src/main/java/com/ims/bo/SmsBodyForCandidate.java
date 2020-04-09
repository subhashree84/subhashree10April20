package com.ims.bo;

import java.util.List;

public class SmsBodyForCandidate {
	
	List<String> candidateIds;
	String message;

	public SmsBodyForCandidate(List<String> candidateIds,String message) {
		super();
		this.message=message;
		this.candidateIds = candidateIds;
	}

}
