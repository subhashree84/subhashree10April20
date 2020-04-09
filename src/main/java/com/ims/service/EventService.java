package com.ims.service;

import java.util.List;

import com.ims.model.Event;

public interface EventService {

	List<Event> getEvents();

	Event getEvent(Long id);

	Event getEvent(String name);

	Event saveEvent(Event event);

	void saveEvents(List<Event> events);

	Event updateEvent(Event event);

	void deleteEvent(Long id);

	boolean isEventExist(Event event);
}
