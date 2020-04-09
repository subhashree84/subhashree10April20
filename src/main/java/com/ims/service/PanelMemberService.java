package com.ims.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ims.model.Event;
import com.ims.model.PanelMember;

public interface PanelMemberService {

	List<PanelMember> getPanelMembers(Long id);
	
	List<PanelMember> getPanelMembers(List<Long> ids);

	PanelMember getPanelMember(Long id);

	PanelMember savePanelMember(PanelMember panelMember);
	
	void savePanelMembers(List<PanelMember> panelMemberList);

	PanelMember updatePanelMember(PanelMember panelMember);

	void deletePanelMember(Long id);
	
	boolean isPanelMemberExist(PanelMember panelMember);
	
	List<PanelMember> uploadFile(Event event, MultipartFile multipartFile) throws IOException;
	
	List<PanelMember> getAvailablePanelMember(Long id);

}
