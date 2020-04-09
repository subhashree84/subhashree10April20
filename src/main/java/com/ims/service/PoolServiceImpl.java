package com.ims.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ims.model.Candidate;
import com.ims.model.Event;

@Service("poolService")
@Transactional
public class PoolServiceImpl implements PoolService {

	@Autowired
	EventService eventService;

	@Autowired
	CandidateService candidateService;
	
	@Autowired
	ProcessService processService;
	
	//@SortDefault(sort = "processId", direction = Sort.Direction.ASC) Sort sort;

	public void poolEvent(String eventPoolReqUrl) {
		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<List<Event>> eventResponse = restTemplate.exchange(eventPoolReqUrl, HttpMethod.GET, entity,
					new ParameterizedTypeReference<List<Event>>() {
					});
			List<Event> events = eventResponse.getBody();
			if (events != null) {
				eventService.saveEvents(events);
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void poolAllCandidates(String candidatePoolReqUri) {
		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

			List<Event> events = eventService.getEvents();

			for (Event event : events) {
				
				ResponseEntity<List<Candidate>> candidateResponse = restTemplate.exchange(candidatePoolReqUri+"?eventId="+event.getEventId()+"&hrId=29&privateKey=1234&pageNo=0",
						HttpMethod.GET, entity, new ParameterizedTypeReference<List<Candidate>>() {
						});
				
				List<Candidate> candidates = candidateService.getCandidates(event.getEventId());

				if (candidateResponse.getBody() != null) {
					List<Candidate> finalCandidates = new ArrayList<Candidate>();
					for (Candidate candidate : candidateResponse.getBody()) {
						candidate.setParentId(1);
						candidate.setEvent(event);
						finalCandidates.add(candidate);

						candidates.stream().filter(c -> c.getEmail().equals(candidate.getEmail())).forEach(c -> {
							c.setCandidateName(candidate.getCandidateName());
							c.setAddress(candidate.getAddress());
							c.setEmail(candidate.getEmail());
							c.setContactNo(candidate.getContactNo());
							c.setGender(candidate.getGender());
							c.setDob(candidate.getDob());

							finalCandidates.remove(candidate);
							finalCandidates.add(c);
						});
					}
					candidateService.saveCandidates(finalCandidates);
				}
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void poolCandidateByEventId(String candidatePoolReqUrl, String eventId) {
		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<List<Candidate>> candidateResponse = restTemplate.exchange(candidatePoolReqUrl,
					HttpMethod.GET, entity, new ParameterizedTypeReference<List<Candidate>>() {
					});

			Event event = eventService.getEvent(Long.parseLong(eventId));
			List<Candidate> candidates = candidateService.getCandidates(Long.parseLong(eventId));

			if (candidateResponse.getBody() != null) {
				List<Candidate> finalCandidates = new ArrayList<Candidate>();
				for (Candidate candidate : candidateResponse.getBody()) {
					candidate.setParentId(1);
					candidate.setEvent(event);
					finalCandidates.add(candidate);

					candidates.stream().filter(c -> c.getEmail().equals(candidate.getEmail())).forEach(c -> {
						c.setCandidateName(candidate.getCandidateName());
						c.setAddress(candidate.getAddress());
						c.setEmail(candidate.getEmail());
						c.setContactNo(candidate.getContactNo());
						c.setGender(candidate.getGender());
						c.setDob(candidate.getDob());

						finalCandidates.remove(candidate);
						finalCandidates.add(c);
					});
				}
				candidateService.saveCandidates(finalCandidates);
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	public void poolAllProcesses(String processPoolReqUrl) {
		try {
			RestTemplate restTemplate = new RestTemplate();

			//Sort sort = @SortDefault(sort = "processId", direction = Sort.Direction.ASC) Sort sort);
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<List<com.ims.model.Process>> processResponse = restTemplate.exchange(processPoolReqUrl,
					HttpMethod.GET, entity, new ParameterizedTypeReference<List<com.ims.model.Process>>() {
					});

			Sort sort = Sort.by(Sort.Order.asc("processId"));
			List<Event> events = eventService.getEvents();

			for (Event event : events) {
				
				List<com.ims.model.Process> processes = processService.getProcesses(event.getEventId(), sort);
				
				if (processResponse.getBody() != null) {
					List<com.ims.model.Process> finalProcesses = new ArrayList<com.ims.model.Process>();
					for (com.ims.model.Process process : processResponse.getBody()) {
						process.setParentId(1);
						process.setEvent(event);
						finalProcesses.add(process);

						processes.stream().filter(p -> p.getProcessSeq() == process.getProcessSeq()).forEach(p -> {
							p.setProcessName(process.getProcessName());
							p.setProcessSeq(process.getProcessSeq());

							finalProcesses.remove(process);
							finalProcesses.add(p);
						});
					}
					processService.saveProcesses(finalProcesses);
				}
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	public void poolProcessByEventId(String processPoolReqUrl, String eventId) {
		try {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.add("user-agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<List<com.ims.model.Process>> processResponse = restTemplate.exchange(processPoolReqUrl,
					HttpMethod.GET, entity, new ParameterizedTypeReference<List<com.ims.model.Process>>() {
					});

			Event event = eventService.getEvent(Long.parseLong(eventId));
			List<com.ims.model.Process> processes = (List<com.ims.model.Process>) processService.getProcess(Long.parseLong(eventId));

			if (processResponse.getBody() != null) {
				List<com.ims.model.Process> finalProcesses = new ArrayList<com.ims.model.Process>();
				for (com.ims.model.Process process : processResponse.getBody()) {
					process.setParentId(1);
					process.setEvent(event);
					finalProcesses.add(process);

					processes.stream().filter(p -> p.getProcessSeq() == process.getProcessSeq()).forEach(p -> {
						p.setProcessName(process.getProcessName());
						p.setProcessSeq(process.getProcessSeq());

						finalProcesses.remove(process);
						finalProcesses.add(p);
					});
				}
				processService.saveProcesses(finalProcesses);
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/*public void poolProcess(String processPoolReqUrl) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<List<com.ims.model.Process>> processResponse = restTemplate.exchange(
					"http://localhost:8080/ims/api/process/", HttpMethod.GET, null,
					new ParameterizedTypeReference<List<com.ims.model.Process>>() {
					});
			List<com.ims.model.Process> processes = processResponse.getBody();
			List<com.ims.model.Process> finalProcesses = null;
			if (processes != null) {
				finalProcesses = new ArrayList<com.ims.model.Process>();
				for (com.ims.model.Process process : processes) {
					System.out.println("Process's Name is" + process.getProcessName());
					finalProcesses.add(process);
				}
				processRepositories.saveAll(finalProcesses);
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}*/

	/*
	 * public void poolProcess() { RestTemplate restTemplate = new RestTemplate();
	 * ResponseEntity<List<Process>> processResponse =
	 * restTemplate.exchange("http://localhost:8080/ims/api/process/",
	 * HttpMethod.GET, null, new ParameterizedTypeReference<List<Process>>() { });
	 * List<Process> processes = processResponse.getBody(); List<Process>
	 * finalProcesses = null; if(processes != null) { finalProcesses = new
	 * ArrayList<Process>(); for(Process process : processes) {
	 * System.out.println("Process's Name is" + process.getProcessName());
	 * finalProcesses.add(process); } processRepositories.saveAll(finalProcesses); }
	 * }
	 */
}
