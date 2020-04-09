package com.ims.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ims.model.Process;
import com.ims.repositories.ProcessRepositories;

@Service("processService")
@Transactional
public class ProcessServiceImpl implements ProcessService {

	@Autowired
	private ProcessRepositories processRepositories;
	
	@Override
	public List<Process> getProcesses(Long id, Sort sort) {
		return processRepositories.findByEventEventId(id,sort);
	}
	
	@Override
	public List<Process> getProcesses(Long eventId, int processSeq) {
		return processRepositories.findByEventEventIdAndProcessSeqLessThanEqual(eventId, processSeq);
	}
	
	//public List<Candidate> findByCandidateIdNotIn(List<Long> ids);
	
	@Override
	public boolean isProcessExist(Process process) {
		return processRepositories.findByProcessName(process.getProcessName()) != null;
	}

	@Override
	public List<Process> saveProcesses(List<Process> processList) {
		return processRepositories.saveAll(processList);
		
	}
	
	@Override
	public Process saveProcess(Process process) {
		return processRepositories.save(process);
	}
	
	@Override
	public Process getProcess(Long id) {
		return processRepositories.findById(id).get();
	}
	
	@Override
	public Process updateProcess(Process process) {
		return processRepositories.save(process);
	}
	
	@Override
	public void deleteProcess(Long id) {
		processRepositories.deleteById(id);
	}

}
