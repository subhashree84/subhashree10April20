package com.ims.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import com.ims.bo.UserCredential;

@Service("imsSessionDataService")
@Transactional
public class SessionDataServiceImpl implements SessionDataService {

	@Autowired
	private SessionRepository sessionRepository;

	// @Autowired
	// private UserCredential userCredential;

	public UserCredential getUserCredential(String id) {
		UserCredential userCredential = null;
		try {
			userCredential = new UserCredential();

			Session session = sessionRepository.findById(id);
			userCredential.setUserSessionId(session.getId());
			userCredential.setUserToken(((List<String>) session.getAttribute("AUTH_TOKEN")).get(0));
		} catch (Exception e) {
			e.getMessage();
		}
		return userCredential;
	}
}
