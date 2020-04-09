package com.ims.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ims.model.Candidate;
import com.ims.model.Event;

public interface CandidateService {

	List<Candidate> getCandidates(Long id);
	
	List<Candidate> findByCandidateIdIn(List<Long> ids);
	
	Candidate findById(Long id);

	Candidate saveCandidate(Candidate candidate);
	
	void saveCandidates(List<Candidate> candidates);

	Candidate updateCandidate(Candidate candidate);

	void deleteCandidateById(Long id);
	
	boolean isCandidateExist(Candidate candidate);
	
	List<Candidate> uploadFile(Event event, MultipartFile multipartFile) throws IOException;

	Candidate findByName(String name);
	
	List<Candidate> getAvailableCandidate(Long id);
}
