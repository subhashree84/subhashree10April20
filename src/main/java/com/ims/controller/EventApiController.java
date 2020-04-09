package com.ims.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.ims.model.Event;
import com.ims.service.EventService;
import com.ims.service.PoolService;
import com.ims.util.CustomErrorType;

@RestController
@Validated
@RequestMapping("/api")
public class EventApiController {

	public static final Logger logger = LoggerFactory.getLogger(EventApiController.class);

	@Autowired
	EventService eventService;

	@Autowired
	PoolService poolService;

	@Value("${app.mapit.eventPoolReqUri}")
	private String eventPoolReqUri;

	@GetMapping("/event/")
	public ResponseEntity<List<Event>> listAllEvents() {
		List<Event> events = eventService.getEvents();

		if (events.isEmpty()) {
			return new ResponseEntity<List<Event>>(HttpStatus.NO_CONTENT);
			// You many decide to return HttpStatus.NOT_FOUND
		}
		return new ResponseEntity<List<Event>>(events, HttpStatus.OK);
	}

	@GetMapping("/event/{id}")
	public ResponseEntity<?> getEvent(@PathVariable("id") long id) {
		logger.info("Fetching Event with id {}", id);
		Event event = eventService.getEvent(id);
		if (event == null) {
			logger.error("Event with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Event with id " + id + " not found"), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Event>(event, HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@PostMapping("/event/")
	public ResponseEntity<?> createEvent(@Valid @RequestBody List<Event> events, UriComponentsBuilder ucBuilder) {
		logger.info("Creating Event : {}", events);

		if (eventService.isEventExist(events.get(0))) {
			logger.error("Unable to create. A Event with name {} already exist", events.get(0).getEventName());
			return new ResponseEntity(
					new CustomErrorType(
							"Unable to create. A Event with name " + events.get(0).getEventName() + " already exist."),
					HttpStatus.CONFLICT);
		}

		Event currentEvent = events.get(0);
		eventService.saveEvent(currentEvent);
		// HttpHeaders headers = new HttpHeaders();
		// headers.setLocation(ucBuilder.path("/api/event/{id}").buildAndExpand(events.get(0).getEventId()).toUri());

		// return new ResponseEntity<String>(headers, HttpStatus.CREATED);
		return new ResponseEntity("Event Created successfully....", HttpStatus.OK);
	}

	@PutMapping("/event/{id}")
	public ResponseEntity<?> updateEvent(@PathVariable("id") long id, @Valid @RequestBody Event event) {
		logger.info("Updating Event with id {}", id);

		Event currentEvent = eventService.getEvent(id);

		if (currentEvent == null) {
			logger.error("Unable to update. Event with id {} not found.", id);
			return new ResponseEntity(new CustomErrorType("Unable to update. Event with id " + id + " not found."),
					HttpStatus.NOT_FOUND);
		}

		currentEvent.setEventName(event.getEventName());

		eventService.updateEvent(currentEvent);
		return new ResponseEntity<Event>(currentEvent, HttpStatus.OK);
	}

	@PostMapping("/poolEvents")
	public ResponseEntity<?> poolEventData(@RequestParam(name = "hrId") String createdId,
			@RequestParam(name = "privateKey") String privateKey, @RequestParam(name = "pagNo") String pageNo) {

		String eventPoolReqUrl = eventPoolReqUri + "?hrId=" + createdId + "&privateKey=" + privateKey + "&pagNo="
				+ pageNo;

		poolService.poolEvent(eventPoolReqUrl);

		return new ResponseEntity("Event pooled successfully....", HttpStatus.OK);
	}
}
