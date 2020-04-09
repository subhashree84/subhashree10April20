package com.ims.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ims.model.Event;
import com.ims.repositories.EventRepositories;

@Service("eventService")
@Transactional
public class EventServiceImpl implements EventService {

	@Autowired
	private EventRepositories eventRepositories;
	
	@Override
	public List<Event> getEvents() {
		return eventRepositories.findAll();
	}

	@Override
	public Event getEvent(Long id) {
		return eventRepositories.getOne(id);
	}
	
	@Override
	public Event getEvent(String name) {
		return eventRepositories.findByEventName(name);
	}

	@Override
	public Event saveEvent(Event event) {
		return eventRepositories.save(event);
	}
	
	@Override
	public void saveEvents(List<Event> events) {
		eventRepositories.saveAll(events);
	}

	@Override
	public Event updateEvent(Event event) {
		return saveEvent(event);
	}

	@Override
	public void deleteEvent(Long id) {
		
	}
	
	@Override
	public boolean isEventExist(Event event) {
		return getEvent(event.getEventName()) != null;
	}
	
	/*
	 * public void poolEvent() { RestTemplate restTemplate = new RestTemplate();
	 * ResponseEntity<List<Event>> eventResponse =
	 * restTemplate.exchange("http://localhost:8080/ims/api/event/", HttpMethod.GET,
	 * null, new ParameterizedTypeReference<List<Event>>() { }); List<Event> events
	 * = eventResponse.getBody(); List<Event> finalEvents = null; if(events != null)
	 * { finalEvents = new ArrayList<Event>(); for(Event event : events) {
	 * System.out.println("Event's Name is" + event.getEventName());
	 * //event.setCreatedId("Biswa "+new Date().getTime()); finalEvents.add(event);
	 * }
	 * 
	 * eventRepositories.saveAll(finalEvents); }
	 * 
	 * }
	 */

}
