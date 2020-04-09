package com.ims.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ims.model.PanelCandidate;
import com.ims.repositories.PanelCandidateRepositories;

@Service("panelCandidateService")
@Transactional
public class PanelCandidateServiceImpl implements PanelCandidateService {

	@Autowired
	private PanelCandidateRepositories panelCandidateRepositories;

	@Override
	public List<PanelCandidate> getCandidateAssignments(Long id) {
		return (List<PanelCandidate>) panelCandidateRepositories.findByIdPanelId(id);
	}
	
	public PanelCandidate getCandidateAssignment(Long panelId, Long candidateId) {
		return (PanelCandidate) panelCandidateRepositories.findByIdPanelIdAndIdCandidateId(panelId, candidateId);
	}
	
	public List<PanelCandidate> getCandidateAssignments(Long panelId, List<Long> candidateIds) {
		return (List<PanelCandidate>) panelCandidateRepositories.findByIdPanelIdAndIdCandidateIdIn(panelId, candidateIds);
	}
	
	public List<PanelCandidate> getCandidateAssignments(List<Long> candidateIds) {
		return (List<PanelCandidate>) panelCandidateRepositories.findByIdCandidateIdIn(candidateIds);
	}
	
	/*
	 * public List<PanelCandidate> findByCandidateIdIn(List<Long> ids) { return
	 * (List<PanelCandidate>) panelCandidateRepositories.findByCandidateIdIn(ids); }
	 */
	
	public void deleteCandidateAssignments(Long panelId, Long candidateId) {
		panelCandidateRepositories.deletePanelCandidateByPanelIdAndCandidateId(panelId, candidateId);
	}
	
	public void deleteCandidateAssignments(List<PanelCandidate> panelCandidates) {
		panelCandidateRepositories.deleteAll(panelCandidates);
	}

	@Override
	public void saveCandidateAssignments(List<PanelCandidate> panelCandidates) {
		panelCandidateRepositories.saveAll(panelCandidates);
	}

}