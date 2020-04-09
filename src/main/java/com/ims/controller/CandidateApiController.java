package com.ims.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.ims.model.Candidate;
import com.ims.model.Event;
import com.ims.model.Panel;
import com.ims.model.PanelCandidate;
import com.ims.service.CandidateService;
import com.ims.service.EventService;
import com.ims.service.PanelCandidateService;
import com.ims.service.PanelService;
import com.ims.service.PoolService;
import com.ims.service.ProcessService;
import com.ims.util.CustomErrorType;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api")
public class CandidateApiController {

	public static final Logger logger = LoggerFactory.getLogger(EventApiController.class);

	@Autowired
	EventService eventService;

	@Autowired
	ProcessService processService;

	@Autowired
	PanelService panelService;
	
	@Autowired
	CandidateService candidateService;
	
	@Autowired
	PanelCandidateService panelCandidateService;

	@Autowired
	PoolService poolService;

	@Value("${app.mapit.candidatePoolReqUri}")
	private String candidatePoolReqUri;

	@GetMapping("/{id}/candidate/")
	public ResponseEntity<List<Candidate>> getCandidates(@PathVariable("id") long id) {

		List<Candidate> candidates = candidateService.getCandidates(id);
		if (candidates.isEmpty()) {
			return new ResponseEntity<List<Candidate>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<Candidate>>(candidates, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/candidate/{id}")
	public ResponseEntity<?> getCandidate(@PathVariable("id") long id) {
		logger.info("Fetching Candidate with id {}", id);
		Candidate candidate = candidateService.findById(id);
		if (candidate == null) {
			logger.error("Candidate with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Candidate with id " + id + " not found"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Candidate>(candidate, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PostMapping("{id}/candidate/")
	public ResponseEntity<?> createCandidate(@Valid @RequestBody List<Candidate> candidates,
			@PathVariable("id") long id, UriComponentsBuilder ucBuilder) {
		logger.info("Creating Candidate : {}", candidates);

		Event event = eventService.getEvent(id);

		if (event == null) {
			logger.error("No Event found with event id {}", event.getEventId());
		} else {

			if (candidateService.isCandidateExist(candidates.get(0))) {
				logger.error("Unable to create. A Candidate {} is already exist", candidates.get(0).getCandidateName());
				return new ResponseEntity(
						new CustomErrorType("Unable to create. A Candidate with email " + candidates.get(0).getEmail()
								+ " and contact no " + candidates.get(0).getContactNo() + " is already exist."),
						HttpStatus.CONFLICT);
			}

			for (Candidate candidate : candidates) {
				candidate.setEvent(event);
			}
			candidateService.saveCandidates(candidates);
			// HttpHeaders headers = new HttpHeaders();
			// headers.setLocation(ucBuilder.path("/api/candidate/{id}").buildAndExpand(candidates.get(0).getCandidateId()).toUri());
			// return new ResponseEntity<String>(headers, HttpStatus.CREATED);
			return new ResponseEntity("Candidate Created successfully.", HttpStatus.OK);
		}

		return new ResponseEntity(new CustomErrorType("Candidate can't be created with event id " + id),
				HttpStatus.NOT_FOUND);
	}

	@SuppressWarnings("unchecked")
	@PutMapping("/candidate/{id}")
	public ResponseEntity<?> updateCandidate(@PathVariable("id") long id, @Valid @RequestBody Candidate candidate) {
		logger.info("Updating Candidate with id {}", id);

		Candidate currentCandidate = candidateService.findById(id);
		if (currentCandidate == null) {
			logger.error("Unable to update. Candidate with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Unable to update. Candidate with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}

		currentCandidate.setCandidateName(candidate.getCandidateName());
		currentCandidate.setAddress(candidate.getAddress());

		candidateService.saveCandidate(currentCandidate);
		return new ResponseEntity("Candidate Updated Successfully.", HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@DeleteMapping("/candidate/{id}")
	public ResponseEntity<?> deleteCandidate(@PathVariable("id") long id) {
		logger.info("Deleting Candidate with id {}", id);

		Candidate currentCandidate = candidateService.findById(id);

		if (currentCandidate == null) {
			logger.error("Unable to delete. Candidate with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Unable to delete. Candidate with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}
		candidateService.deleteCandidateById(id);
		return new ResponseEntity("Candidate Deleted Successfully.", HttpStatus.OK);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/{id}/panelCandidate")
	public ResponseEntity<?> getPanelCandidateAssignments(@PathVariable("id") long id) {
		logger.info("Fetching Panel with id {}", id);
		// Panel panel = panelService.findById(id);
		List<PanelCandidate> panelCandidate = panelCandidateService.getCandidateAssignments(id);
		if (panelCandidate == null) {
			logger.error("Candidates for Panel with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Candidate for Panel with id " + id + " not found"),
					HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(panelCandidate, HttpStatus.OK);
	}
	
	@GetMapping("/{id}/availableCandidate")
	public ResponseEntity<?> getAvailableCandidate(@PathVariable("id") long id) {
		logger.info("Fetching Available Candidates with process id {}", id);
		com.ims.model.Process process = processService.getProcess(id);
		List<com.ims.model.Process> previousProcesses = processService.getProcesses(process.getEvent().getEventId(),
				process.getProcessSeq());

		List<Candidate> availableCandidates = new ArrayList<Candidate>();

		if (process.getProcessSeq() == 1) {
			availableCandidates = candidateService.getCandidates(process.getEvent().getEventId());

			List<Candidate> candidates = new ArrayList<Candidate>();
			previousProcesses.forEach(previousProcess -> previousProcess.getPanels()
					.forEach(panel -> panel.getPanelCandidates().stream()
							.filter(panelCandidate -> panelCandidate.getAttendance() > 0)
							.forEach(panelCandidate -> candidates.add(panelCandidate.getCandidate()))));

			availableCandidates.removeAll(candidates);

		} else {
			Set<Candidate> candidates = new HashSet<Candidate>();

			previousProcesses.forEach(previousProcess -> previousProcess.getPanels().forEach(panel -> panel
					.getPanelCandidates().stream()
					.filter(panelCandidate -> panelCandidate.getStatus() == 2
							&& panelCandidate.getPanel().getProcess().getProcessSeq() == process.getProcessSeq() - 1)
					.forEach(panelCandidate -> candidates.add(panelCandidate.getCandidate()))));

			previousProcesses.forEach(previousProcess -> previousProcess.getPanels().forEach(panel -> panel
					.getPanelCandidates().stream()
					.filter(panelCandidate -> panelCandidate.getAttendance() > 0
							&& panelCandidate.getPanel().getProcess().getProcessSeq() == process.getProcessSeq())
					.forEach(panelCandidate -> candidates.remove(panelCandidate.getCandidate()))));

			/*
			 * process.getPanels() .forEach(panel -> panel.getPanelCandidates().stream()
			 * .filter(panelCandidate -> panelCandidate.getAttendance() > 0)
			 * .forEach(candidate -> candidates.remove(candidate)));
			 */

			availableCandidates = candidates.stream().collect(Collectors.toList());
		}

		List<Candidate> finalAvailableCandidates = availableCandidates;

		for (int i = 0; i < availableCandidates.size(); i++) {
			finalAvailableCandidates.get(i).getPanelCandidates()
					.removeIf(panelCandidate -> (panelCandidate.getPanel().getProcessId() != id));
		}

		/*
		 * availableCandidates.forEach(candidate ->
		 * candidate.getPanelCandidates().stream() .filter(panelCandidate ->
		 * panelCandidate.getPanel().getProcessId() != id) .forEach(panelCandidate ->
		 * candidate.getPanelCandidates().remove(panelCandidate)));
		 */

		return new ResponseEntity<List<Candidate>>(finalAvailableCandidates, HttpStatus.OK);
	}

	@GetMapping("/{id}/shortlistedCandidate")
	public ResponseEntity<?> getShortlistedCandidate(@PathVariable("id") long id) {
		logger.info("Fetching Shortlisted Candidates with process id {}", id);
		com.ims.model.Process process = processService.getProcess(id);
		List<com.ims.model.Process> currentAndPreviousProcesses = processService
				.getProcesses(process.getEvent().getEventId(), process.getProcessSeq());

		List<Candidate> shortlistedCandidates = new ArrayList<Candidate>();

		if (process.getProcessSeq() == 1) {
			shortlistedCandidates = candidateService.getCandidates(process.getEvent().getEventId());
		} else {
			Set<Candidate> candidates = new HashSet<Candidate>();

			currentAndPreviousProcesses.forEach(currentAndPreviousProcess -> currentAndPreviousProcess.getPanels()
					.forEach(panel -> panel.getPanelCandidates().stream()
							.filter(panelCandidate -> panelCandidate.getStatus() == 2 && panelCandidate.getPanel()
									.getProcess().getProcessSeq() == process.getProcessSeq() - 1)
							.forEach(panelCandidate -> candidates.add(panelCandidate.getCandidate()))));

			shortlistedCandidates = candidates.stream().collect(Collectors.toList());
		}

		List<Candidate> finalShortlistedCandidates = shortlistedCandidates;

		for (int i = 0; i < shortlistedCandidates.size(); i++) {
			finalShortlistedCandidates.get(i).getPanelCandidates()
					.removeIf(panelCandidate -> (panelCandidate.getPanel().getProcessId() != id));
		}

		return new ResponseEntity<List<Candidate>>(finalShortlistedCandidates, HttpStatus.OK);
	}

	@GetMapping("/availableCandidateForReAssignment/{id}")
	public ResponseEntity<?> getAvailableCandidateForReAssignment(@PathVariable("id") long id) {
		logger.info("Fetching Shortlisted Candidates with Panel id {}", id);

		Panel panel = panelService.getPanel(id);

		com.ims.model.Process process = processService.getProcess(panel.getProcessId());
		List<com.ims.model.Process> currentAndPreviousProcesses = processService
				.getProcesses(process.getEvent().getEventId(), process.getProcessSeq());

		List<Candidate> shortlistedCandidates = new ArrayList<Candidate>();

		if (process.getProcessSeq() == 1) {
			shortlistedCandidates = candidateService.getCandidates(process.getEvent().getEventId());
		} else {
			Set<Candidate> candidates = new HashSet<Candidate>();

			currentAndPreviousProcesses.forEach(currentAndPreviousProcess -> currentAndPreviousProcess.getPanels()
					.forEach(p -> p.getPanelCandidates().stream()
							.filter(panelCandidate -> (panelCandidate.getStatus() == 2 && panelCandidate.getPanel()
									.getProcess().getProcessSeq() == process.getProcessSeq() - 1))
							.forEach(panelCandidate -> candidates.add(panelCandidate.getCandidate()))));

			shortlistedCandidates = candidates.stream().collect(Collectors.toList());
		}

		Set<Candidate> candidatesAlreadyProcessedInOtherPanel = new HashSet<Candidate>();

		for (int i = 0; i < shortlistedCandidates.size(); i++) {
			shortlistedCandidates.get(i).getPanelCandidates()
					.removeIf(panelCandidate -> (panelCandidate.getPanel().getProcessId() != panel.getProcessId()));

			shortlistedCandidates.get(i).getPanelCandidates().stream()
					.filter(pc -> (pc.getPanelId() != panel.getPanelId() && pc.getStatus() > 0))
					.forEach(pc -> candidatesAlreadyProcessedInOtherPanel.add(pc.getCandidate()));
		}

		shortlistedCandidates.removeAll(candidatesAlreadyProcessedInOtherPanel);

		return new ResponseEntity<List<Candidate>>(shortlistedCandidates, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PostMapping("{id}/candidate/upload/")
	public ResponseEntity<?> uploadFile(@PathVariable("id") long id,
			@RequestPart(value = "file") MultipartFile multiPartFile) throws IOException {
		logger.info("Uploading Candidate with id {}", id);
		Event event = eventService.getEvent(id);

		if (event == null) {
			logger.error("Unable to upload Candidate with id {} not found.", id);
		}
		candidateService.uploadFile(event, multiPartFile);
		return new ResponseEntity("Candidates Uploaded Successfully.", HttpStatus.OK);
	}

	@PostMapping("/poolCandidatesByEventId")
	public ResponseEntity<?> poolCandidateDataByEventId(@RequestParam(name = "eventId") String eventId,
			@RequestParam(name = "hrId") String createdId, @RequestParam(name = "privateKey") String privateKey,
			@RequestParam(name = "pageNo") String pageNo) {

		String candidatePoolReqUrl = candidatePoolReqUri + "?eventId=" + eventId + "&hrId=" + createdId + "&privateKey="
				+ privateKey + "&pageNo=" + pageNo;

		poolService.poolCandidateByEventId(candidatePoolReqUrl, eventId);

		return new ResponseEntity("Candidate pooled successfully....", HttpStatus.OK);
	}
	
	@PostMapping("/poolCandidates")
	public ResponseEntity<?> poolCandidateData() {

		poolService.poolAllCandidates(candidatePoolReqUri);
		return new ResponseEntity("Candidate pooled successfully....", HttpStatus.OK);
	}
}
