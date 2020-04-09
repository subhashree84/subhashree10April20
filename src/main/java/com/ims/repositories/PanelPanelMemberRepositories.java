package com.ims.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ims.model.PanelPanelMember;
import com.ims.model.PanelPanelMemberId;

@Repository
public interface PanelPanelMemberRepositories extends JpaRepository<PanelPanelMember, PanelPanelMemberId>{

	List<PanelPanelMember> findByIdPanelId(Long id);
	
	List<PanelPanelMember> findByIdPanelMemberIdIn(List<Long> panelMemberIds);
	
	List<PanelPanelMember> findByIdPanelIdAndIdPanelMemberIdIn(Long panelId, List<Long> panelMemberIds);
	
	@Modifying
    @Query(value = "DELETE FROM TMAP_IMS_PANEL_PANELMEMBER WHERE PANEL_ID = :panelId AND PANELMEMBER_ID = :panelMemberId", nativeQuery = true)
	void deletePanelPanelMemberByPanelIdAndPanelMemberId(Long panelId, Long panelMemberId);
}
