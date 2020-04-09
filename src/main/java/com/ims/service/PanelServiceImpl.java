package com.ims.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ims.model.Panel;
import com.ims.repositories.PanelRepositories;

@Service("panelService")
@Transactional
public class PanelServiceImpl implements PanelService {

	@Autowired
	private PanelRepositories panelRepositories;

	public List<Panel> getPanels(Long id, Sort sort) {
		return panelRepositories.findByProcessProcessId(id, sort);
	}
	
	@Override
	public Panel getPanel(Long id) {
		return panelRepositories.findById(id).get();
	}

	@Override
	public void savePanel(Panel panel) {
		panelRepositories.save(panel);
	}

	public void saveAll(List<Panel> panels) {
		panelRepositories.saveAll(panels);
	}

	@Override
	public void updatePanel(Panel panel) {
		panelRepositories.save(panel);
	}

	@Override
	public void deletePanelById(Long id) {
		panelRepositories.deleteById(id);
	}
	
	@Override
	public void deletePanels(List<Panel> panels) {
		panelRepositories.deleteAll(panels);
	}

	@Override
	public boolean isPanelExist(Panel panel) {
		return panelRepositories.findByPanelName(panel.getPanelName()) != null;
	}

	@Override
	public Long countPanelByProcessId(Long id) {
		return panelRepositories.countPanelByProcessId(id);
	}

}
