package com.ims.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ims.model.PanelPanelMember;
import com.ims.repositories.PanelPanelMemberRepositories;

@Service("panelPanelMemberService")
@Transactional
public class PanelPanelMemberServiceImpl implements PanelPanelMemberService {

	@Autowired
	private PanelPanelMemberRepositories panelPanelMemberRepositories;

	@Override
	public ArrayList<PanelPanelMember> getPanelMemberAssignments(Long id) {
		return (ArrayList<PanelPanelMember>) panelPanelMemberRepositories.findByIdPanelId(id);
	}
	
	public List<PanelPanelMember> getPanelMemberAssignments(List<Long> ids) {
		return (List<PanelPanelMember>) panelPanelMemberRepositories.findByIdPanelMemberIdIn(ids);
	}
	
	@Override
	public List<PanelPanelMember> getPanelMemberAssignments(Long panelId, List<Long> panelMemberIds) {
		return (List<PanelPanelMember>) panelPanelMemberRepositories.findByIdPanelIdAndIdPanelMemberIdIn(panelId, panelMemberIds);
	}
	
	public void deletePanelMemberAssignments(List<PanelPanelMember> panelPanelMembers) {
		panelPanelMembers.forEach(panelPanelMember -> panelPanelMemberRepositories.delete(panelPanelMember));
	}
	
	public void deletePanelMemberAssignments(Long panelId, Long panelMemberId) {
		panelPanelMemberRepositories.deletePanelPanelMemberByPanelIdAndPanelMemberId(panelId, panelMemberId);
	}

	@Override
	public void savePanelMemberAssignments(List<PanelPanelMember> panelPanelMembers) {
		panelPanelMemberRepositories.saveAll(panelPanelMembers);
	}
}