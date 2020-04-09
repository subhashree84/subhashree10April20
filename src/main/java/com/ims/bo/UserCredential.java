package com.ims.bo;

import lombok.Data;

@Data
public class UserCredential {
	
	private String userSessionId;

	private String userToken;
}
