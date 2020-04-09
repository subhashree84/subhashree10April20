package com.ims.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ims.model.PanelCandidate;
import com.ims.model.PanelCandidateId;

@Repository
public interface PanelCandidateRepositories extends JpaRepository<PanelCandidate, PanelCandidateId>{

	List<PanelCandidate> findByIdPanelId(Long id);
	
	List<PanelCandidate> findByIdCandidateIdIn(List<Long> candidateIds);
	
	List<PanelCandidate> findByIdPanelIdAndIdCandidateIdIn(Long panelId, List<Long> candidateIds);
	
	PanelCandidate findByIdPanelIdAndIdCandidateId(Long panelId, Long candidateId);
	
	@Modifying
    @Query(value = "DELETE FROM TMAP_IMS_PANEL_CANDIDATE WHERE PANEL_ID = :panelId AND CANDIDATE_ID = :candidateId", nativeQuery = true)
	void deletePanelCandidateByPanelIdAndCandidateId(@Param("panelId")Long panelId, @Param("candidateId")Long candidateId);
	
	//List<PanelCandidate> findByCandidateIdIn(List<Long> ids);
	/*
	 * Candidate findByCandidateName(String name); Candidate
	 * findByEmailAndContactNo(String email, String contactNo); public
	 * List<Candidate> findByCandidateIdIn(List<Long> ids);
	 */
}
