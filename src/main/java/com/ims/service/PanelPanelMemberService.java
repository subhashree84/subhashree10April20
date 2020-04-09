package com.ims.service;

import java.util.List;

import com.ims.model.PanelPanelMember;

public interface PanelPanelMemberService {

	List<PanelPanelMember> getPanelMemberAssignments(Long id);
	
	List<PanelPanelMember> getPanelMemberAssignments(List<Long> ids);
	
	List<PanelPanelMember> getPanelMemberAssignments(Long panelId, List<Long> panelMemberIds);
	
	void deletePanelMemberAssignments(List<PanelPanelMember> panelPanelMembers);
	
	void deletePanelMemberAssignments(Long panelId, Long panelMemberId);

	void savePanelMemberAssignments(List<PanelPanelMember> panelPanelMembers);
}
