package com.ims.service;

import java.util.List;

import com.ims.model.PanelCandidate;

public interface PanelCandidateService {

	List<PanelCandidate> getCandidateAssignments(Long panelId);
	
	List<PanelCandidate> getCandidateAssignments(List<Long> candidateIds);
	
	PanelCandidate getCandidateAssignment(Long panelId, Long candidateId);
	
	List<PanelCandidate> getCandidateAssignments(Long panelId, List<Long> candidateIds);
	
	void deleteCandidateAssignments(List<PanelCandidate> panelCandidates);
	
	void deleteCandidateAssignments(Long panelId, Long candidateId);

	void saveCandidateAssignments(List<PanelCandidate> panelCandidates);
}
