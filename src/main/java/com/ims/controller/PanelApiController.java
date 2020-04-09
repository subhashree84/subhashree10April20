package com.ims.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.ims.bo.AddMoreCandidateInfo;
import com.ims.bo.AttendanceInfo;
import com.ims.bo.AutoAssignmentInfo;
import com.ims.bo.FeedbackInfo;
import com.ims.bo.PanelBO;
import com.ims.bo.PanelInfo;
import com.ims.model.Candidate;
import com.ims.model.Panel;
import com.ims.model.PanelCandidate;
import com.ims.model.PanelCandidateId;
import com.ims.model.PanelMember;
import com.ims.model.PanelPanelMember;
import com.ims.model.PanelPanelMemberId;
import com.ims.service.CandidateService;
import com.ims.service.MailContentBuilder;
import com.ims.service.PanelCandidateService;
import com.ims.service.PanelMemberService;
import com.ims.service.PanelPanelMemberService;
import com.ims.service.PanelService;
import com.ims.service.ProcessService;
import com.ims.util.CustomErrorType;
import com.ims.util.SmsUtility;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class PanelApiController {

	public static final Logger logger = LoggerFactory.getLogger(PanelApiController.class);

	@Autowired
	PanelService panelService;

	@Autowired
	ProcessService processService;

	@Autowired
	CandidateService candidateService;

	@Autowired
	PanelMemberService panelMemberService;

	@Autowired
	PanelCandidateService panelCandidateService;

	@Autowired
	PanelPanelMemberService panelPanelMemberService;

	@Autowired
	private MailContentBuilder mailContentBuilder;

	@Value("${sms.url.value}")
	private String smsUrl;

	@Value("${sms.secretappkey.value}")
	private String smsSecretappkey;
	
	@Value("${spring.hr.mobile}")
	private String hrMobile;

	@GetMapping("{id}/panel/")
	public ResponseEntity<List<Panel>> getPanels(@PathVariable("id") long id, @SortDefault(sort = "panelName", direction = Sort.Direction.ASC) Sort sort) {
		List<Panel> panels = panelService.getPanels(id, sort);
		if (panels.isEmpty()) {
			return new ResponseEntity<List<Panel>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Panel>>(panels, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/panel/{id}")
	public ResponseEntity<?> getPanel(@PathVariable("id") long id) {
		logger.info("Fetching Panel with id {}", id);
		Panel panel = panelService.getPanel(id);
		if (panel == null) {
			logger.error("Panel with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Panel with id " + id + " not found"), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Panel>(panel, HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/{id}/panel")
	public ResponseEntity<?> createPanel(@PathVariable("id") long id,
			@Validated(PanelInfo.class) @RequestBody PanelBO panelBO, UriComponentsBuilder ucBuilder) {

		if (panelBO.getCandidateIds().size() == 0) {
			return new ResponseEntity("Panel can't be created without candidate.", HttpStatus.OK);
		}

		List<PanelCandidate> existingPanelCandidates = panelCandidateService
				.getCandidateAssignments(panelBO.getCandidateIds());

		ArrayList<String> inProcessCandidates = new ArrayList<String>();
		existingPanelCandidates.stream().filter(
				panelCandidate -> panelCandidate.getPanel().getProcessId() == id && panelCandidate.getAttendance() == 1)
				.forEach(panelCandidate -> inProcessCandidates.add(panelCandidate.getCandidate().getCandidateName()));

		if (inProcessCandidates.size() > 0) {
			return new ResponseEntity(
					new CustomErrorType(
							"Panel Can't be created. Candidates are already in process : " + inProcessCandidates),
					HttpStatus.FORBIDDEN);
		} else {

			ArrayList<PanelCandidate> availableCandidates = new ArrayList<PanelCandidate>();
			existingPanelCandidates.stream().filter(panelCandidate -> panelCandidate.getPanel().getProcessId() == id)
					.forEach(panelCandidate -> availableCandidates.add(panelCandidate));

			panelCandidateService.deleteCandidateAssignments(availableCandidates);

			List<PanelPanelMember> existingPanelPanelMembers = panelPanelMemberService
					.getPanelMemberAssignments(panelBO.getPanelMemberIds());
			panelPanelMemberService.deletePanelMemberAssignments(existingPanelPanelMembers);

			com.ims.model.Process process = processService.getProcess(id);
			List<Candidate> candidates = candidateService.findByCandidateIdIn(panelBO.getCandidateIds());
			List<PanelMember> panelMembers = panelMemberService.getPanelMembers(panelBO.getPanelMemberIds());

			Panel panel = new Panel(panelBO.getPanelName(), panelBO.getRoomNo());
			panel.setProcess(process);
			panelService.savePanel(panel);

			ArrayList<PanelCandidate> panelCandidates = new ArrayList<PanelCandidate>();
			candidates.forEach(candidate -> panelCandidates.add(new PanelCandidate(
					new PanelCandidateId(panel.getPanelId(), candidate.getCandidateId()), panel, candidate)));
			panelCandidateService.saveCandidateAssignments(panelCandidates);

			ArrayList<PanelPanelMember> panelPanelMembers = new ArrayList<PanelPanelMember>();
			panelMembers.forEach(panelMember -> panelPanelMembers.add(new PanelPanelMember(
					new PanelPanelMemberId(panel.getPanelId(), panelMember.getPanelMemberId()), panel, panelMember)));
			panelPanelMemberService.savePanelMemberAssignments(panelPanelMembers);
		}

		return new ResponseEntity("Panel created successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PutMapping("/panel/{id}")
	public ResponseEntity<?> updatePanel(@PathVariable("id") long id,
			@Validated(PanelInfo.class) @RequestBody PanelBO panelBO, UriComponentsBuilder ucBuilder) {

		Panel currentPanel = panelService.getPanel(id);

		List<PanelCandidate> currentPanelCandidates = panelCandidateService.getCandidateAssignments(id);

		List<PanelCandidate> existingPanelCandidates = currentPanel.getProcess().getPanelCandidates();

		/*
		 * panelCandidateService
		 * .getCandidateAssignments(panelBO.getCandidateIds()).stream() .filter(pc1 ->
		 * pc1.getProcessId() == currentPanel.getProcessId())
		 * .collect(Collectors.toList());
		 */

		ArrayList<String> inProcessCandidates = new ArrayList<String>();
		existingPanelCandidates.stream()
				.filter(pc2 -> pc2.getStatus() == 1 && panelBO.getCandidateIds().contains(pc2.getCandidateId()))
				.forEach(panelCandidate -> inProcessCandidates.add(panelCandidate.getCandidate().getCandidateName()
						+ " (" + panelCandidate.getCandidate().getCandidateId() + ")"));

		if (inProcessCandidates.size() > 0) {
			return new ResponseEntity(
					new CustomErrorType(
							"Candidates are already in process : " + inProcessCandidates.toArray().toString()),
					HttpStatus.FORBIDDEN);
		} else {

			currentPanel.setPanelName(panelBO.getPanelName());
			currentPanel.setRoomNo(panelBO.getRoomNo());
			panelService.savePanel(currentPanel);

			List<PanelCandidate> deletePanelCandidates = new ArrayList<PanelCandidate>();
			existingPanelCandidates.stream()
					.filter(pc3 -> (pc3.getPanelId() == id && pc3.getStatus() == 0
							&& !panelBO.getCandidateIds().contains(pc3.getCandidateId()))
							|| (pc3.getPanelId() != id && panelBO.getCandidateIds().contains(pc3.getCandidateId())
									&& pc3.getStatus() == 0))
					.forEach(panelCandidate -> deletePanelCandidates.add(panelCandidate));

			deletePanelCandidates.forEach(
					pc4 -> panelCandidateService.deleteCandidateAssignments(pc4.getPanelId(), pc4.getCandidateId()));

			// panelCandidateService.deleteCandidateAssignments(deleteCandidates);

			List<Long> latestCandidates = new ArrayList<Long>();
			panelCandidateService.getCandidateAssignments(panelBO.getCandidateIds()).stream()
					.filter(pc1 -> pc1.getProcessId() == currentPanel.getProcessId()
							&& panelBO.getCandidateIds().contains(pc1.getCandidateId()) && pc1.getStatus() > 1)
					.forEach(pc1 -> latestCandidates.add(pc1.getCandidateId()));

			ArrayList<PanelCandidate> panelCandidates = new ArrayList<PanelCandidate>();
			panelBO.getCandidateIds().stream().filter(candidateId -> !latestCandidates.contains(candidateId)).forEach(
					candidateId -> panelCandidates.add(new PanelCandidate(new PanelCandidateId(id, candidateId),
							currentPanel, candidateService.findById(candidateId))));

			panelCandidateService.saveCandidateAssignments(panelCandidates);

			List<PanelPanelMember> currentPanelPanelMembers = currentPanel.getProcess().getPanelPanelMembers();

			/*
			 * panelPanelMemberService.getPanelMemberAssignments(id) .stream().filter(ppm ->
			 * ppm.getPanelId() == currentPanel.getProcessId())
			 * .collect(Collectors.toList());
			 */

			ArrayList<PanelPanelMember> deletePanelMembers = new ArrayList<PanelPanelMember>();
			currentPanelPanelMembers.stream()
					.filter(panelPanelMember -> (panelPanelMember.getPanelId() == id
							&& panelPanelMember.getStatus() == 0
							&& !panelBO.getPanelMemberIds().contains(panelPanelMember.getPanelMemberId()))
							|| (panelPanelMember.getPanelId() != id && panelPanelMember.getStatus() == 0
									&& panelBO.getPanelMemberIds().contains(panelPanelMember.getPanelMemberId())))
					.forEach(panelPanelMember -> deletePanelMembers.add(panelPanelMember));

			deletePanelMembers.forEach(ppm2 -> panelPanelMemberService.deletePanelMemberAssignments(ppm2.getPanelId(),
					ppm2.getPanelMemberId()));
			// panelPanelMemberService.deletePanelMemberAssignments(deletePanelMembers);

			List<Long> latestPanelMembers = new ArrayList<Long>();
			panelPanelMemberService.getPanelMemberAssignments(panelBO.getPanelMemberIds()).stream()
					.filter(pc1 -> pc1.getPanelId() == id)
					.forEach(pc1 -> latestPanelMembers.add(pc1.getPanelMemberId()));

			ArrayList<PanelPanelMember> panelPanelMembers = new ArrayList<PanelPanelMember>();
			panelBO.getPanelMemberIds().stream().filter(panelMemberId -> !latestPanelMembers.contains(panelMemberId))
					.forEach(panelMemberId -> panelPanelMembers
							.add(new PanelPanelMember(new PanelPanelMemberId(id, panelMemberId), currentPanel,
									panelMemberService.getPanelMember(panelMemberId))));

			panelPanelMemberService.savePanelMemberAssignments(panelPanelMembers);
		}

		return new ResponseEntity("Panel updated successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@DeleteMapping("/panel/{id}")
	public ResponseEntity<?> deletePanel(@PathVariable("id") long id) {
		logger.info("Deleting Panel with id {}", id);

		Panel currentPanel = panelService.getPanel(id);

		if (currentPanel == null) {
			logger.error("Unable to delete. Panel with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Unable to delete. Panel with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}

		List<PanelCandidate> panelCandidates = panelCandidateService.getCandidateAssignments(id);
		List<PanelPanelMember> panelPanelMembers = panelPanelMemberService.getPanelMemberAssignments(id);

		ArrayList<String> inProcessCandidates = new ArrayList<String>();
		panelCandidates.stream().filter(panelCandidate -> panelCandidate.getAttendance() > 0)
				.forEach(panelCandidate -> inProcessCandidates.add(panelCandidate.getCandidate().getCandidateName()));

		if (inProcessCandidates.size() > 0) {
			return new ResponseEntity(
					new CustomErrorType(
							"Panel can't be deleted. Candidates are already in process : " + inProcessCandidates),
					HttpStatus.FORBIDDEN);
		} else {
			panelCandidateService.deleteCandidateAssignments(panelCandidates);
			panelPanelMemberService.deletePanelMemberAssignments(panelPanelMembers);
			panelService.deletePanelById(id);
		}
		return new ResponseEntity("Panel deleted successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PutMapping("/panel/{id}/attendance")
	public ResponseEntity<?> updateAttendance(@PathVariable("id") long id,
			@Validated(AttendanceInfo.class) @RequestBody PanelBO panelBO, UriComponentsBuilder ucBuilder) {

		List<Long> candidateIds = new ArrayList<Long>();
		candidateIds.addAll(panelBO.getPresentCandidateIds());
		candidateIds.addAll(panelBO.getAbsentCandidateIds());

		List<PanelCandidate> panelCandidates = panelCandidateService.getCandidateAssignments(id, candidateIds);

		List<Long> processCompletedCandidateIds = new ArrayList<Long>();
		panelCandidates.stream().filter(pc -> pc.getStatus() > 1)
				.forEach(pc -> processCompletedCandidateIds.add(pc.getCandidateId()));

		panelBO.getPresentCandidateIds().removeAll(processCompletedCandidateIds);
		panelBO.getAbsentCandidateIds().removeAll(processCompletedCandidateIds);

		panelCandidates.forEach(panelCandidate -> {
			if (panelBO.getPresentCandidateIds().contains(panelCandidate.getCandidateId())) {
				panelCandidate.setAttendance(1);
				panelCandidate.setStatus(1);
			}
			
			if (panelBO.getAbsentCandidateIds().contains(panelCandidate.getCandidateId())) {
				panelCandidate.setAttendance(0);
				panelCandidate.setStatus(0);
			}
		});

		List<PanelPanelMember> panelPanelMembers = panelPanelMemberService.getPanelMemberAssignments(id);

		panelPanelMembers.forEach(panelPanelMember -> {
			if(panelBO.getPresentCandidateIds().size() > 0)
				panelPanelMember.setStatus(1);
			else if(processCompletedCandidateIds.size() > 0)
				panelPanelMember.setStatus(2);
			else
				panelPanelMember.setStatus(0);
		});

		panelCandidateService.saveCandidateAssignments(panelCandidates);
		panelPanelMemberService.savePanelMemberAssignments(panelPanelMembers);

		return new ResponseEntity("Candidate attendance updated successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PutMapping("/panel/{id}/completeFeedback")
	public ResponseEntity<?> updateStatus(@PathVariable("id") long id, @RequestBody PanelBO panelBO,
			UriComponentsBuilder ucBuilder) throws IOException {
		String candidateStatus = "";
		Panel panel = panelService.getPanel(id);
		String eventName = panel.getEventName();
		String panelDetails = panel.getProcessName();
		List<PanelCandidate> panelCandidatesForFeedbackUpdate = panelCandidateService.getCandidateAssignments(id,
				panelBO.getCandidateIds());

		panelCandidatesForFeedbackUpdate.forEach(panelCandidate -> {
			panelCandidate.setCompleteFeedback(1);
		});
		panelCandidateService.saveCandidateAssignments(panelCandidatesForFeedbackUpdate);
				
		PanelCandidate singlePanelCandidate = panelCandidateService.getCandidateAssignment(id,
				panelBO.getCandidateIds().get(0));

		List<String> hrNumberList = new ArrayList<>();
		hrNumberList.add(hrMobile);

		List<String> contactNumberList = new ArrayList<>();
		Candidate candidate = candidateService.findById(panelBO.getCandidateIds().get(0));
		contactNumberList.add(candidate.getContactNo());

		SmsUtility smsUtility = new SmsUtility();
		if (singlePanelCandidate.getStatus() == 2) {
			candidateStatus = "cleared";
			String smsBody = mailContentBuilder.smsBuildForSelectedOrRejected(eventName, panelDetails,
					candidate.getCandidateName(), candidateStatus, "candidateStatusTemplate");
			smsUtility.sendSms(smsBody, contactNumberList, smsUrl, smsSecretappkey);
		} else {
			candidateStatus = "not cleared";
			String smsBody = mailContentBuilder.smsBuildForSelectedOrRejected(eventName, panelDetails,
					candidate.getCandidateName(), candidateStatus, "candidateStatusTemplate");
			smsUtility.sendSms(smsBody, contactNumberList, smsUrl, smsSecretappkey);
		}

		// for hr sms
		String smsBody = mailContentBuilder.smsBuildForSelectedOrRejected(eventName, panelDetails,
				candidate.getCandidateName(), candidateStatus, "hrManagerSmsTemplate");
		smsUtility.sendSms(smsBody, hrNumberList, smsUrl, smsSecretappkey);
		return new ResponseEntity("Candidate feedback updated successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PutMapping("/panel/{id}/feedback")
	public ResponseEntity<?> updateFeedback(@PathVariable("id") long id,
			@Validated(FeedbackInfo.class) @RequestBody PanelBO panelBO, UriComponentsBuilder ucBuilder) {

		List<PanelCandidate> panelCandidatesForFeedbackUpdate = panelCandidateService.getCandidateAssignments(id,
				panelBO.getCandidateIds());

		panelCandidatesForFeedbackUpdate.forEach(panelCandidate -> {
			panelCandidate.setStatus(panelBO.getStatus());
			panelCandidate.setFeedback(panelBO.getFeedback());
		});

		panelCandidateService.saveCandidateAssignments(panelCandidatesForFeedbackUpdate);

		//
		List<PanelCandidate> panelCandidates = panelCandidateService.getCandidateAssignments(id);
		List<PanelCandidate> removePanelCandidates = new ArrayList<PanelCandidate>();
		panelCandidates.stream().filter(panelCandidate -> panelCandidate.getFeedback() != null)
				.forEach(panelCandidate -> removePanelCandidates.add(panelCandidate));

		panelCandidates.removeAll(removePanelCandidates);
		//

		List<PanelPanelMember> panelPanelMembers = panelPanelMemberService.getPanelMemberAssignments(id);
		panelPanelMembers.forEach(panelPanelMember -> {
			panelPanelMember
					.setStatus(panelCandidates.size() == 0 ? 2 : (panelBO.getStatus() > 2 ? 2 : panelBO.getStatus()));
		});

		panelPanelMemberService.savePanelMemberAssignments(panelPanelMembers);

		return new ResponseEntity("Candidate feedback updated successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/{id}/panels")
	public ResponseEntity<?> createPanels(@PathVariable("id") long id,
			@Validated(AutoAssignmentInfo.class) @RequestBody PanelBO panelBO, UriComponentsBuilder ucBuilder) {

		List<Candidate> candidates = candidateService.getAvailableCandidate(id);
		List<PanelMember> panelMembers = panelMemberService.getAvailablePanelMember(id).stream()
				.filter(panelMember -> panelMember.getPanelPanelMember().size() == 0).collect(Collectors.toList());

		final AtomicInteger counter = new AtomicInteger();

		final Collection<List<Candidate>> candidateBatches = candidates.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / panelBO.getCandidateCntPerPanel()))
				.values();

		final Collection<List<PanelMember>> panelMemberBatches = panelMembers.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / panelBO.getPanelMemberCntPerPanel()))
				.values();

		com.ims.model.Process process = processService.getProcess(id);
		Long totalPanelCount = panelService.countPanelByProcessId(id);

		for (int i = 0; i < panelBO.getPanelCnt(); i++) {

			Panel panel = new Panel("Panel " + (++totalPanelCount), "");
			panel.setProcess(process);
			panelService.savePanel(panel);

			if (i < candidateBatches.size()) {
				List<PanelCandidate> panelCandidates = new ArrayList<PanelCandidate>();

				((List<Candidate>) candidateBatches.toArray()[i]).forEach(candidate -> panelCandidates
						.add(new PanelCandidate(new PanelCandidateId(panel.getPanelId(), candidate.getCandidateId()),
								panel, candidate)));
				panelCandidateService.saveCandidateAssignments(panelCandidates);
			}

			if (i < panelMemberBatches.size()) {
				List<PanelPanelMember> panelPanelMembers = new ArrayList<PanelPanelMember>();
				((List<PanelMember>) panelMemberBatches.toArray()[i]).forEach(panelMember -> panelPanelMembers.add(
						new PanelPanelMember(new PanelPanelMemberId(panel.getPanelId(), panelMember.getPanelMemberId()),
								panel, panelMember)));

				panelPanelMemberService.savePanelMemberAssignments(panelPanelMembers);
			}

			System.out.println("Panel " + (totalPanelCount) + " created ........................................");
		}

		return new ResponseEntity("Panels created successfully.", HttpStatus.OK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PutMapping("/panel/{id}/addMoreCandidates")
	public ResponseEntity<?> addMoreCandidates(@PathVariable("id") long id,
			@Validated(AddMoreCandidateInfo.class) @RequestBody PanelBO panelBO, UriComponentsBuilder ucBuilder) {

		if (panelBO.getCandidateIds().size() == 0) {
			return new ResponseEntity("Atleast one candidate is required to assign.", HttpStatus.OK);
		}

		Panel panel = panelService.getPanel(id);

		List<PanelCandidate> existingPanelCandidates = panelCandidateService
				.getCandidateAssignments(panelBO.getCandidateIds());

		ArrayList<PanelCandidate> availableCandidates = new ArrayList<PanelCandidate>();
		existingPanelCandidates.stream()
				.filter(panelCandidate -> panelCandidate.getPanel().getProcessId() == panel.getProcessId())
				.forEach(panelCandidate -> availableCandidates.add(panelCandidate));

		List<PanelCandidate> deleteCandidates = availableCandidates.stream()
				.filter(panelCandidate -> panelCandidate.getStatus() == 0).collect(Collectors.toList());

		panelCandidateService.deleteCandidateAssignments(deleteCandidates);

		ArrayList<PanelCandidate> panelCandidates = new ArrayList<PanelCandidate>();
		panelBO.getCandidateIds()
				.forEach(candidateId -> panelCandidates.add(new PanelCandidate(new PanelCandidateId(id, candidateId),
						panel, candidateService.findById(candidateId))));

		panelCandidateService.saveCandidateAssignments(panelCandidates);

		return new ResponseEntity("Candidates assigned successfully.", HttpStatus.OK);
	}
}
