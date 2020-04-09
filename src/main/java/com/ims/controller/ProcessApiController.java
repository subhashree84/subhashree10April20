package com.ims.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.ims.model.Event;
import com.ims.model.PanelCandidate;
import com.ims.service.EventService;
import com.ims.service.PanelCandidateService;
import com.ims.service.PanelPanelMemberService;
import com.ims.service.PanelService;
import com.ims.service.ProcessService;
import com.ims.util.CustomErrorType;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class ProcessApiController {

	public static final Logger logger = LoggerFactory.getLogger(ProcessApiController.class);
	
	@Autowired
	ProcessService processService;
	
	@Autowired
	PanelCandidateService panelCandidateService;
	
	@Autowired
	PanelPanelMemberService panelPanelMemberService;
	
	@Autowired
	PanelService panelService;
	
	@Autowired
	EventService eventService;
	
	@GetMapping("/{id}/process/")
	public ResponseEntity<List<com.ims.model.Process>> getProcesses(@PathVariable("id") long id, @SortDefault(sort = "processId", direction = Sort.Direction.ASC) Sort sort) {
		List<com.ims.model.Process> processes = processService.getProcesses(id, sort);
		if (processes.isEmpty()) {
			return new ResponseEntity<List<com.ims.model.Process>>(HttpStatus.NO_CONTENT);
			// You many decide to return HttpStatus.NOT_FOUND
		}
		return new ResponseEntity<List<com.ims.model.Process>>(processes, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/process/{id}")
	public ResponseEntity<?> getProcessById(@PathVariable("id") long id) {
		logger.info("Fetching Process with id {}", id);
		com.ims.model.Process process = processService.getProcess(id);
		if (process == null) {
			logger.error("Process with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Process with id " + id + " not found"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<com.ims.model.Process>(process, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("{id}/process/")
	public ResponseEntity<?> createProcess(@Valid @RequestBody List<com.ims.model.Process> processes,
			@PathVariable("id") long id, UriComponentsBuilder ucBuilder) {
		logger.info("Creating Process : {}", processes);

		Event event = eventService.getEvent(id);

		if (event == null) {
			logger.error("No Event found with event id {}", event.getEventId());
		} else {

			if (processService.isProcessExist(processes.get(0))) {
				logger.error("Unable to create. A Process {} is already exist",
						processes.get(0).getProcessName());
			}

			for (com.ims.model.Process process : processes) {
				process.setEvent(event);
			}
			List<com.ims.model.Process> allProcess =processService.saveProcesses(processes);
			System.out.println("Event Name ......................"+processes.get(0).getEvent().getEventName());
			HttpHeaders headers = new HttpHeaders();
			headers.setLocation(
					ucBuilder.path("/api/process/{id}").buildAndExpand(processes.get(0).getProcessId()).toUri());
			if (allProcess.isEmpty()) {
				return new ResponseEntity<List<com.ims.model.Process>>(HttpStatus.NO_CONTENT);
				// You many decide to return HttpStatus.NOT_FOUND
			}
			return new ResponseEntity<List<com.ims.model.Process>>(allProcess, HttpStatus.OK);

			//return new ResponseEntity<String>(headers, HttpStatus.CREATED);
		}

		return new ResponseEntity(new CustomErrorType("Process can't be created with event id " + id),
				HttpStatus.NOT_FOUND);
	}
	
	@PutMapping("/process/{id}")
	public ResponseEntity<?> updateProcess(@PathVariable("id") long id, @RequestBody com.ims.model.Process process) {
		logger.info("Updating Process with id {}", id);

		com.ims.model.Process currentProcess = processService.getProcess(id);
		if (currentProcess == null) {
			logger.error("Unable to update. Process with id {} not found.", id);
			return new ResponseEntity<Object>(new CustomErrorType("Unable to update. Process with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}

		currentProcess.setProcessName(process.getProcessName());
		currentProcess.setStartDate(process.getStartDate());
		currentProcess.setProcessSeq(process.getProcessSeq());
		currentProcess.setEndDate(process.getEndDate());
		processService.saveProcess(currentProcess);
		return new ResponseEntity<com.ims.model.Process>(currentProcess, HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@DeleteMapping("/process/{id}")
	public ResponseEntity<?> deleteProcess(@PathVariable("id") long id) {
		logger.info("Deleting Process with id {}", id);

		com.ims.model.Process currentProcess = processService.getProcess(id);
		
		if (currentProcess == null) {
			logger.error("Unable to delete. Process with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Unable to delete. Process with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}
		
		List<PanelCandidate> existingPanelCandidates = currentProcess.getPanelCandidates();

		List<String> inProcessCandidates = new ArrayList<String>();
		existingPanelCandidates.stream()
				.filter(panelCandidate -> panelCandidate.getAttendance() > 0)
				.forEach(panelCandidate -> inProcessCandidates.add(panelCandidate.getCandidate().getCandidateName()));
		
		if (inProcessCandidates.size() > 0) {
			return new ResponseEntity(new CustomErrorType("Panel Can't be deleted. Candidates are already in process : " + inProcessCandidates),
					HttpStatus.FORBIDDEN);
		} else {
			panelCandidateService.deleteCandidateAssignments(currentProcess.getPanelCandidates());
			panelPanelMemberService.deletePanelMemberAssignments(currentProcess.getPanelPanelMembers());
			panelService.deletePanels(currentProcess.getPanels());
			processService.deleteProcess(id);
		}
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

}
