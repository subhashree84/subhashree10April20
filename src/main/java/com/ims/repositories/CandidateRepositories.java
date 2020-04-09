package com.ims.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ims.model.Candidate;

@Repository
public interface CandidateRepositories extends JpaRepository<Candidate, Long>{

	public List<Candidate> findByEventEventId(Long id);
	Candidate findByCandidateName(String name);
	Candidate findByEmailAndContactNo(String email, String contactNo);
	public List<Candidate> findByCandidateIdIn(List<Long> ids);
	public List<Candidate> findByCandidateIdNotIn(List<Long> ids);
}
