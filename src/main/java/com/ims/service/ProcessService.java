package com.ims.service;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.ims.model.Process;

public interface ProcessService {

	List<com.ims.model.Process> getProcesses(Long id, Sort sort);
	
	List<com.ims.model.Process> getProcesses(Long id, int processSeq);
	
	boolean isProcessExist(com.ims.model.Process process);
	
	List<Process> saveProcesses(List<com.ims.model.Process> processList);
	
	com.ims.model.Process saveProcess(com.ims.model.Process process);
	
	com.ims.model.Process getProcess(Long id);
	
	com.ims.model.Process updateProcess(com.ims.model.Process process);
	
	void deleteProcess(Long id);
	
}
