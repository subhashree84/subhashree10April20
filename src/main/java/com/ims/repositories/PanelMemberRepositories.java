package com.ims.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ims.model.PanelMember;

@Repository
public interface PanelMemberRepositories extends JpaRepository<PanelMember, Long>{
	List<PanelMember> findByEventEventId(Long id);
	PanelMember findByPanelMemberName(String panelMemberName);
	List<PanelMember> findByPanelMemberIdIn(List<Long> ids);
	PanelMember findByEmailAndContactNo(String email, String contactNo);
}
