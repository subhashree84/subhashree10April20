package com.ims.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ims.model.Event;
import com.ims.model.PanelMember;
import com.ims.repositories.PanelMemberRepositories;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

@Service("panelMemberService")
@Transactional
public class PanelMemberServiceImpl implements PanelMemberService {

	@Autowired
	private PanelMemberRepositories panelMemberRepositories;
	
	@Autowired
	ProcessService processService;
	
	@Autowired
	PanelMemberService panelMemberService;
	
	public List<PanelMember> getPanelMembers(Long id) {
		return panelMemberRepositories.findByEventEventId(id);
	}
	
	@Override
	public List<PanelMember> getPanelMembers(List<Long> ids) {
		return panelMemberRepositories.findByPanelMemberIdIn(ids);
	}

	@Override
	public PanelMember getPanelMember(Long id) {
		return panelMemberRepositories.findById(id).get();
	}

	@Override
	public PanelMember savePanelMember(PanelMember panelMember) {
		return panelMemberRepositories.save(panelMember);
	}
	
	public void saveAll(List<PanelMember> panelMembers) {
		panelMemberRepositories.saveAll(panelMembers);
	}
	
	public void savePanelMembers(List<PanelMember> panelMemberList) {
		panelMemberRepositories.saveAll(panelMemberList);
	}

	@Override
	public PanelMember updatePanelMember(PanelMember panelMember) {
		return savePanelMember(panelMember);
	}
	
	@Override
	public boolean isPanelMemberExist(PanelMember panelMember) {
		return panelMemberRepositories.findByEmailAndContactNo(panelMember.getEmail(), panelMember.getContactNo()) != null;
	}

	@Override
	public void deletePanelMember(Long id) {
		panelMemberRepositories.deleteById(id);
	}
	
	public List<PanelMember> uploadFile(Event event, MultipartFile multipartFile) throws IOException {

		File file = convertMultiPartToFile(multipartFile);

		List<PanelMember> mandatoryMissedList = new ArrayList<PanelMember>();

		try (Reader reader = new FileReader(file);) {
			@SuppressWarnings("unchecked")
			CsvToBean<PanelMember> csvToBean = new CsvToBeanBuilder<PanelMember>(reader).withType(PanelMember.class)
					.withIgnoreLeadingWhiteSpace(true).build();
			List<PanelMember> panelMemberList = csvToBean.parse();

			Iterator<PanelMember> panelMemberListClone = panelMemberList.iterator();

			while (panelMemberListClone.hasNext()) {

				PanelMember panelMember = panelMemberListClone.next();
				panelMember.setEvent(event);

				if (panelMember.getContactNo() == null || panelMember.getContactNo().isEmpty()
						|| panelMember.getPanelMemberName() == null || panelMember.getPanelMemberName().isEmpty()) {
					mandatoryMissedList.add(panelMember);
					panelMemberListClone.remove();
				}
			}

			savePanelMembers(panelMemberList);
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
	
	public List<PanelMember> getAvailablePanelMember(Long id) {
		com.ims.model.Process process = processService.getProcess(id);
		List<com.ims.model.Process> previousProcesses = processService
				.getProcesses(process.getEvent().getEventId(), process.getProcessSeq());

		List<PanelMember> availablePanelMembers = panelMemberService.getPanelMembers(process.getEvent().getEventId());

		List<PanelMember> panelMembers = new ArrayList<PanelMember>();
		previousProcesses.forEach(previousProcess -> previousProcess.getPanels()
				.forEach(panel -> panel.getPanelPanelMembers().stream()
						.filter(panelPanelMember -> (process.getEndDate().isAfter(LocalDateTime.now()) || panelPanelMember.getStatus() ==1))
						.forEach(panelPanelMember -> panelMembers.add(panelPanelMember.getPanelMember()))));

		availablePanelMembers.removeAll(panelMembers);
		
		return availablePanelMembers;
	}
}
