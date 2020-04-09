package com.ims.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ims.model.Event;

@Repository
public interface EventRepositories extends JpaRepository<Event, Long> {
	Event findByEventName(String name);
}
