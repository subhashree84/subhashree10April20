package com.ims.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ims.model.Panel;

@Repository
public interface PanelRepositories extends JpaRepository<Panel, Long>{
	
	Panel findByPanelName(String name);
	
	List<Panel> findByProcessProcessId(Long id, Sort sort);
	
	@Query("select count(p) from Panel p where p.process.processId = ?1")
    Long countPanelByProcessId(Long id);
}
