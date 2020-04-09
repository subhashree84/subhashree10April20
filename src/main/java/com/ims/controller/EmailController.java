package com.ims.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ims.bo.EmailBodyForCandidate;
import com.ims.model.Candidate;
import com.ims.model.Panel;
import com.ims.model.PanelCandidate;
import com.ims.model.PanelMember;
import com.ims.model.PanelPanelMember;
import com.ims.service.CandidateService;
import com.ims.service.MailContentBuilder;
import com.ims.service.PanelMemberService;
import com.ims.service.PanelService;

@CrossOrigin(origins = "*", allowedHeaders = "*")

@RestController
@RequestMapping("/api")
public class EmailController {
	public static final Logger logger = LoggerFactory.getLogger(EventApiController.class);

	@Autowired
	private PanelService panelService;
	@Autowired
	private MailContentBuilder mailContentBuilder;
	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private PanelMemberService panelMemberService;

	@Value("${spring.mail.mailSubject}")
	private String mailSubject;

	@Value("${spring.mail.bccAllMail}")
	private String bccAllMail;

	@Value("${sms.url.value}")
	private String smsUrl;

	@Value("${sms.secretappkey.value}")
	private String smsSecretappkey;

	@GetMapping("/sendmail/{panelId}")
	public ResponseEntity<?> sendEmailToBothCandidateAndPanelMember(@PathVariable("panelId") long panelId,
			@RequestHeader("feedbackUrl") String feedbackURL) throws MessagingException, IOException {
		logger.info("Fetching panelId with panelId {}", panelId);
		Panel panel = panelService.getPanel(panelId);
		String panelDetails = panel.getPanelName() + ", " + panel.getCreatedDate() + ", ";
		logger.info("Panel with id {}  found.", panelId);
		Set<PanelCandidate> assignedCandidate = panel.getPanelCandidates();
		Set<PanelPanelMember> assignedPanelMemeber = panel.getPanelPanelMembers();
		Iterator<PanelCandidate> allAssignedCandidate = assignedCandidate.iterator();
		while (allAssignedCandidate.hasNext()) {
			PanelCandidate panelCandidate = allAssignedCandidate.next();
			Candidate candidate = candidateService.findById(panelCandidate.getCandidateId());
			String emailBody = mailContentBuilder.build(panelDetails, panelCandidate.getCandidateName(), feedbackURL,
					"candidateMailTemplate");
			sendEmailWithAttachment(emailBody, candidate.getEmail());
		}

		Iterator<PanelPanelMember> allAssignedPanelMemeber = assignedPanelMemeber.iterator();
		while (allAssignedPanelMemeber.hasNext()) {
			PanelPanelMember panelPanelMember = allAssignedPanelMemeber.next();
			PanelMember panelMemeber = panelMemberService.getPanelMember(panelPanelMember.getPanelMemberId());
			String emailBody = mailContentBuilder.build(panelDetails, panelPanelMember.getPanelMemberName(),
					feedbackURL, "panelMemberMailTemplate");
			sendEmailWithAttachment(emailBody, panelMemeber.getEmail());
		}
		return new ResponseEntity<Panel>(panel, HttpStatus.OK);
	}

	@PostMapping("/sendMailandSms/candidate/{panelId}")
	public ResponseEntity<?> sendEmailAndSmsToCandidate(@PathVariable("panelId") long panelId,
			@RequestBody EmailBodyForCandidate emailAndSmsBodyForCandidate) throws MessagingException, IOException {
		logger.info("Fetching panelId with panelId {}", panelId);
		Panel panel = panelService.getPanel(panelId);
		String panelDetails = panel.getPanelName() + ", " + panel.getCreatedDate() + ", ";
		logger.info("Panel with id {}  found.", panelId);
		Set<PanelCandidate> assignedCandidate = panel.getPanelCandidates();
		Iterator<PanelCandidate> allAssignedCandidate = assignedCandidate.iterator();
		while (allAssignedCandidate.hasNext()) {
			List<String> candidateIdContactNumber = new ArrayList<>();
			PanelCandidate panelCandidate = allAssignedCandidate.next();
			Candidate candidate = candidateService.findById(panelCandidate.getCandidateId());
			if (emailAndSmsBodyForCandidate.getCandidateIds().contains(panelCandidate.getCandidateId())) {
				String emailBody = mailContentBuilder.candidateMailTemplate(panelDetails,
						panelCandidate.getCandidateName(), "candidateMailTemplate");
				sendEmailWithAttachment(emailBody, candidate.getEmail());
				candidateIdContactNumber.add(candidate.getContactNo());
				String smsBody = mailContentBuilder.smsBuild(panelDetails, panelCandidate.getCandidateName(),
						"candidateSms");
				sendSms(smsBody, candidateIdContactNumber);
			}
		}
		return new ResponseEntity<Panel>(panel, HttpStatus.OK);
	}

	private void sendSms(String message, List<String> contactNmuber) throws IOException {
		JSONObject messageBody = new JSONObject();
		messageBody.put("message", message);
		messageBody.put("phoneNumber", contactNmuber);
		CloseableHttpClient httpClient1 = HttpClientBuilder.create().build();
		try {
			HttpPost request = new HttpPost(smsUrl);
			StringEntity params = new StringEntity(messageBody.toString());
			request.addHeader("content-type", "application/json");
			request.addHeader("secretappkey", smsSecretappkey);
			request.setEntity(params);
			httpClient1.execute(request);
		} catch (Exception ex) {
		} finally {
			httpClient1.close();
		}

	}

	@GetMapping("/sendMailandSms/panelMember/{panelId}")
	public ResponseEntity<?> sendEmailToAllPanelMember(@PathVariable("panelId") long panelId,
			@RequestHeader("feedbackUrl") String feedbackURL) throws MessagingException, IOException {
		logger.info("Fetching panelId with panelId {}", panelId);
		Panel panel = panelService.getPanel(panelId);
		String panelDetails = panel.getPanelName() + ", " + panel.getCreatedDate() + ", ";
		logger.info("Panel with id {}  found.", panelId);
		Set<PanelPanelMember> assignedPanelMemeber = panel.getPanelPanelMembers();
		Iterator<PanelPanelMember> allAssignedPanelMemeber = assignedPanelMemeber.iterator();
		while (allAssignedPanelMemeber.hasNext()) {
			List<String> panelMemberContactNumber = new ArrayList<>();
			PanelPanelMember panelPanelMember = allAssignedPanelMemeber.next();
			PanelMember panelMemeber = panelMemberService.getPanelMember(panelPanelMember.getPanelMemberId());
			String emailBody = mailContentBuilder.build(panelDetails, panelPanelMember.getPanelMemberName(),
					feedbackURL, "panelMemberMailTemplate");
			panelMemberContactNumber.add(panelMemeber.getContactNo());
			sendEmailWithAttachment(emailBody, panelMemeber.getEmail());
			String smsBody = mailContentBuilder.smsBuild(panelDetails, panelPanelMember.getPanelMemberName(),
					"panelMemberSms");
			sendSms(smsBody, panelMemberContactNumber);
		}
		return new ResponseEntity<Panel>(panel, HttpStatus.OK);
	}

	private void sendEmailWithAttachment(String mailBody, String email) throws MessagingException, IOException {
		MimeMessage msg = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		helper.setTo(email);
		helper.setBcc(bccAllMail);
		helper.setSubject(mailSubject);
		helper.setText("<h1>Check attachment for image!</h1>", true);
		helper.setText(mailBody, true);

		javaMailSender.send(msg);

	}

}
