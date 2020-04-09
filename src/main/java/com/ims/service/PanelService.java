package com.ims.service;

import java.util.List;

import org.springframework.data.domain.Sort;

import com.ims.model.Panel;

public interface PanelService {

	List<Panel> getPanels(Long id, Sort sort);

	Panel getPanel(Long id);

	void savePanel(Panel panel);
	
	void saveAll(List<Panel> panels);

	void updatePanel(Panel panel);

	void deletePanelById(Long id);
	
	void deletePanels(List<Panel> panels);
	
	boolean isPanelExist(Panel panel);
	
	Long countPanelByProcessId(Long id);

}
