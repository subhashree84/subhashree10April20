package com.ims.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessRepositories extends JpaRepository<com.ims.model.Process, Long>{
	
	List<com.ims.model.Process> findByEventEventId(Long id, Sort sort);
	List<com.ims.model.Process> findByEventEventIdAndProcessSeqLessThanEqual(Long id, int processSeq);
	com.ims.model.Process findByProcessName(String processName);
}
