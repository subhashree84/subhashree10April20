package com.ims.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ims.model.Candidate;
import com.ims.model.Event;
import com.ims.repositories.CandidateRepositories;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service("candidateService")
@Transactional
public class CandidateServiceImpl implements CandidateService {

	@Autowired
	private CandidateRepositories candidateRepositories;

	@Autowired
	ProcessService processService;

	@Override
	public List<Candidate> getCandidates(Long id) {
		return candidateRepositories.findByEventEventId(id);
	}

	@Override
	public List<Candidate> findByCandidateIdIn(List<Long> ids) {
		return candidateRepositories.findByCandidateIdIn(ids);
	}

	@Override
	public Candidate findById(Long id) {
		return candidateRepositories.getOne(id);
	}

	@Override
	public Candidate findByName(String name) {
		return candidateRepositories.findByCandidateName(name);
	}

	@Override
	public Candidate saveCandidate(Candidate candidate) {
		return candidateRepositories.save(candidate);
	}

	public void saveCandidates(List<Candidate> candidates) {
		candidateRepositories.saveAll(candidates);
	}

	@Override
	public Candidate updateCandidate(Candidate candidate) {
		return candidateRepositories.save(candidate);
	}

	@Override
	public void deleteCandidateById(Long id) {
		candidateRepositories.deleteById(id);
	}

	@Override
	public boolean isCandidateExist(Candidate candidate) {
		return candidateRepositories.findByEmailAndContactNo(candidate.getEmail(), candidate.getContactNo()) != null;
	}

	public List<Candidate> uploadFile(Event event, MultipartFile multipartFile) throws IOException {

		File file = convertMultiPartToFile(multipartFile);

		List<Candidate> mandatoryMissedList = new ArrayList<Candidate>();

		try (Reader reader = new FileReader(file);) {
			@SuppressWarnings("unchecked")
			CsvToBean<Candidate> csvToBean = new CsvToBeanBuilder<Candidate>(reader).withType(Candidate.class)
					.withIgnoreLeadingWhiteSpace(true).build();
			List<Candidate> candidateList = csvToBean.parse();

			Iterator<Candidate> candidateListClone = candidateList.iterator();

			while (candidateListClone.hasNext()) {

				Candidate candidate = candidateListClone.next();

				if (candidate.getContactNo() == null || candidate.getContactNo().isEmpty()
						|| candidate.getCandidateName() == null || candidate.getCandidateName().isEmpty()) {
					mandatoryMissedList.add(candidate);
					candidateListClone.remove();
				}
			}

			for (Candidate candidate : candidateList) {
				candidate.setEvent(event);
			}
			saveCandidates(candidateList);
		}
		return mandatoryMissedList;
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	public List<Candidate> getAvailableCandidate(Long id) {
		com.ims.model.Process process = processService.getProcess(id);
		List<com.ims.model.Process> previousProcesses = processService
				.getProcesses(process.getEvent().getEventId(), process.getProcessSeq());

		List<Candidate> availableCandidates = new ArrayList<Candidate>();

		if (process.getProcessSeq() == 1) {
			availableCandidates = getCandidates(process.getEvent().getEventId());

			List<Candidate> candidates = new ArrayList<Candidate>();
			previousProcesses.forEach(previousProcess -> previousProcess.getPanels()
					.forEach(panel -> panel.getPanelCandidates().stream()
							.filter(panelCandidate -> panelCandidate.getAttendance() > 0)
							.forEach(panelCandidate -> candidates.add(panelCandidate.getCandidate()))));

			availableCandidates.removeAll(candidates);

		} else {
			Set<Candidate> candidates = new HashSet<Candidate>();

			previousProcesses.forEach(previousProcess -> previousProcess.getPanels()
					.forEach(panel -> panel.getPanelCandidates().stream()
							.filter(panelCandidate -> (panelCandidate.getStatus() == 2 && panelCandidate.getProcessSeq() == process.getProcessSeq() - 1))
							.forEach(panelCandidate -> candidates.add(panelCandidate.getCandidate()))));

			process.getPanels()
					.forEach(panel -> panel.getPanelCandidates().stream()
							.filter(panelCandidate -> panelCandidate.getAttendance() > 0 && panelCandidate.getProcessSeq()== process.getProcessSeq())
							.forEach(candidate -> candidates.remove(candidate)));

			availableCandidates = candidates.stream().collect(Collectors.toList());
		}
		return availableCandidates;
	}
}
