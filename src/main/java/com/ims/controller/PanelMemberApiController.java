package com.ims.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.ims.model.Event;
import com.ims.model.Panel;
import com.ims.model.PanelMember;
import com.ims.model.PanelPanelMember;
import com.ims.service.EventService;
import com.ims.service.PanelMemberService;
import com.ims.service.PanelPanelMemberService;
import com.ims.service.PanelService;
import com.ims.service.ProcessService;
import com.ims.util.CustomErrorType;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api")
public class PanelMemberApiController {

	public static final Logger logger = LoggerFactory.getLogger(PanelMemberApiController.class);

	@Autowired
	EventService eventService;

	@Autowired
	ProcessService processService;
	
	@Autowired
	PanelService panelService;
	
	@Autowired
	PanelMemberService panelMemberService;
	
	@Autowired
	PanelPanelMemberService panelPanelMemberService;

	@GetMapping("/{id}/panelMember/")
	public ResponseEntity<List<PanelMember>> getPanelMembers(@PathVariable("id") Long id) {
		List<PanelMember> panelMembers = panelMemberService.getPanelMembers(id);
		if (panelMembers.isEmpty()) {
			return new ResponseEntity<List<PanelMember>>(HttpStatus.NO_CONTENT);
			// You many decide to return HttpStatus.NOT_FOUND
		}
		return new ResponseEntity<List<PanelMember>>(panelMembers, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/panelMember/{id}")
	public ResponseEntity<?> getPanelMember(@PathVariable("id") long id) {
		logger.info("Fetching PanelMember with id {}", id);
		PanelMember panelMember = panelMemberService.getPanelMember(id);
		if (panelMember == null) {
			logger.error("PanelMember with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("PanelMember with id " + id + " not found"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<PanelMember>(panelMember, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("/{id}/panelMember")
	public ResponseEntity<?> createPanelMember(@Valid @RequestBody List<PanelMember> panelMembers, @PathVariable("id") long id,
			UriComponentsBuilder ucBuilder) {

		Event event = eventService.getEvent(id);

		if (event == null) {
			logger.error("No Event found with event id {}", event.getEventId());
		} else {

			if (panelMemberService.isPanelMemberExist(panelMembers.get(0))) {
				logger.error("Unable to create. A PanelMember {} is already exist",
						panelMembers.get(0).getPanelMemberName());
				return new ResponseEntity(new CustomErrorType(
						"Unable to create. A Panel Member with Email " + panelMembers.get(0).getEmail()
								+ " and contact no " + panelMembers.get(0).getContactNo() + " is already exist."),
						HttpStatus.CONFLICT);
			}

			for (PanelMember panelMember : panelMembers) {
				panelMember.setEvent(event);
			}
			panelMemberService.savePanelMembers(panelMembers);
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(ucBuilder.path("/api/panelMember/{id}")
					.buildAndExpand(panelMembers.get(0).getPanelMemberId()).toUri());

			return new ResponseEntity<String>(headers, HttpStatus.CREATED);
		}

		return new ResponseEntity("Panel Member Created successfully.", HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PutMapping("/panelMember/{id}")
	public ResponseEntity<?> updatePanelMember(@PathVariable("id") long id, @Valid @RequestBody PanelMember panelMember) {
		logger.info("Updating PanelMember with id {}", id);

		PanelMember currentPanelMember = panelMemberService.getPanelMember(id);

		if (currentPanelMember == null) {
			logger.error("Unable to update. PanelMember with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Unable to upate. PanelMember with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}

		// currentPanelMember.setPanelPanelMember(panelMember.getPanelPanelMember());
		currentPanelMember.setContactNo(panelMember.getContactNo());
		currentPanelMember.setEmail(panelMember.getEmail());
		currentPanelMember.setPanelMemberName(panelMember.getPanelMemberName());

		panelMemberService.updatePanelMember(currentPanelMember);
		return new ResponseEntity("Panel Member Updated successfully.", HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@DeleteMapping("/panelMember/{id}")
	public ResponseEntity<?> deletePanelMember(@PathVariable("id") long id) {
		logger.info("Deleting PanelMember with id {}", id);

		PanelMember currentPanelMember = panelMemberService.getPanelMember(id);

		if (currentPanelMember == null) {
			logger.error("Unable to delete. PanelMember with id {} not found.", id);
			return new ResponseEntity(
					new CustomErrorType("Unable to delete. PanelMember with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}
		panelMemberService.deletePanelMember(id);
		return new ResponseEntity("Panel Member Deleted successfully.", HttpStatus.OK);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/{id}/panelPanelMember")
	public ResponseEntity<?> getpanelPanelMemberAssignments(@PathVariable("id") long id) {
		logger.info("Fetching Panel with id {}", id);
		// Panel panel = panelService.findById(id);
		List<PanelPanelMember> panelPanelMember = panelPanelMemberService.getPanelMemberAssignments(id);
		if (panelPanelMember == null) {
			logger.error("PanelMembers for Panel with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("PanelMember for Panel with id " + id + " not found"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(panelPanelMember, HttpStatus.OK);
	}

	@GetMapping("/{id}/availablePanelMember")
	public ResponseEntity<?> getAvailablePanelMember(@PathVariable("id") long id) {

		logger.info("Fetching Available PanelMembers with process id {}", id);

		com.ims.model.Process process = processService.getProcess(id);
		List<com.ims.model.Process> currentAndPreviousProcesses = processService
				.getProcesses(process.getEvent().getEventId(), process.getProcessSeq());

		List<PanelMember> availablePanelMembers = panelMemberService.getPanelMembers(process.getEvent().getEventId());

		Set<PanelMember> panelMembers = new HashSet<PanelMember>();
		/*currentAndPreviousProcesses.forEach(currentAndPreviousProcess -> currentAndPreviousProcess.getPanels()
				.forEach(panel -> panel.getPanelPanelMembers().stream()
						.filter(panelPanelMember -> process.getEndDate().isAfter(LocalDateTime.now()))
						.forEach(panelPanelMember -> panelMembers.add(panelPanelMember.getPanelMember()))));

		availablePanelMembers.removeAll(panelMembers);*/
		
		currentAndPreviousProcesses.forEach(currentAndPreviousProcess -> currentAndPreviousProcess.getPanels()
				.forEach(panel -> panel.getPanelPanelMembers().stream()
						.filter(panelPanelMember -> (panelPanelMember.getStatus() == 1))
						.forEach(panelPanelMember -> panelMembers.add((panelPanelMember.getPanelMember())))));
								
		availablePanelMembers.removeAll(panelMembers);
		
		return new ResponseEntity<List<PanelMember>>(availablePanelMembers, HttpStatus.OK);
	}
	
	@GetMapping("/availablePanelMemberForReassignment/{id}")
	public ResponseEntity<?> getAvailablePanelMemberForReassignment(@PathVariable("id") long id) {

		logger.info("Fetching Available PanelMembers For Reassignment with Panel id {}", id);

		Panel panel = panelService.getPanel(id);
		
		com.ims.model.Process process = processService.getProcess(panel.getPanelId());
		List<com.ims.model.Process> currentAndPreviousProcesses = processService
				.getProcesses(process.getEvent().getEventId(), process.getProcessSeq());

		List<PanelMember> availablePanelMembers = panelMemberService.getPanelMembers(process.getEvent().getEventId());

		Set<PanelMember> panelMembers = new HashSet<PanelMember>();
				currentAndPreviousProcesses.forEach(currentAndPreviousProcess -> currentAndPreviousProcess.getPanels()
				.forEach(pnl -> pnl.getPanelPanelMembers().stream()
						.filter(panelPanelMember -> (panelPanelMember.getPanelId() != id && panelPanelMember.getStatus() == 1))
						.forEach(panelPanelMember -> panelMembers.add((panelPanelMember.getPanelMember())))));
					
		availablePanelMembers.removeAll(panelMembers);
		
		return new ResponseEntity<List<PanelMember>>(availablePanelMembers, HttpStatus.OK);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping("{id}/panelMember/upload")
	public ResponseEntity<?> uploadFile(@PathVariable("id") long id,
			@RequestPart(value = "file") MultipartFile multiPartFile) throws IOException {
		logger.info("Uploading PanelMember with id {}", id);
		Event event = eventService.getEvent(id);

		if (event == null) {
			logger.error("Unable to upload Candidate with id {} not found.", id);
		}
		panelMemberService.uploadFile(event, multiPartFile);
		return new ResponseEntity("Panel Members Uploaded Successfully.", HttpStatus.OK);
	}
}
