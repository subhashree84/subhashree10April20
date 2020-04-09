package com.ims.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ims.bo.UserCredential;
import com.ims.service.SessionDataService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class SessionDataController {

	@Autowired
	SessionDataService sessionDataService;

	@PostMapping("/persistSessionData")
	public ResponseEntity<?> persistSessionData(@RequestParam("auth_token") String authToken,
			HttpServletRequest request) {

		List<String> attributes = (List<String>) request.getSession().getAttribute("AUTH_TOKEN");

		if (attributes == null) {
			attributes = new ArrayList<>();
			request.getSession().setAttribute("AUTH_TOKEN", attributes);
		}

		attributes.add(authToken);
		request.getSession().setAttribute("AUTH_TOKEN", attributes);

		return new ResponseEntity("Session persisted successfully.", HttpStatus.OK);
	}

	@GetMapping("/getSessionData")
	public ResponseEntity<List<String>> getSessionData(HttpSession session) {
		@SuppressWarnings("unchecked")
		List<String> messages = (List<String>) session.getAttribute("AUTH_TOKEN");

		if (messages == null) {
			messages = new ArrayList<>();
		}

		return new ResponseEntity<List<String>>(messages, HttpStatus.OK);
	}

	@GetMapping("/getUserCredential")
	public ResponseEntity<?> getUserCredential(HttpSession session) {

		UserCredential userCredential = sessionDataService.getUserCredential(session.getId());

		return new ResponseEntity<UserCredential>(userCredential, HttpStatus.OK);
	}

	@PostMapping("/destroySessionData")
	public ResponseEntity<?> destroySession(HttpServletRequest request) {
		request.getSession().invalidate();
		return new ResponseEntity("Session destroyed successfully.", HttpStatus.OK);
	}
}