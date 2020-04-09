package com.ims.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Data
@Embeddable
@JsonIgnoreType
@Setter @Getter @NoArgsConstructor
public class PanelPanelMemberId implements Serializable {
	private Long panelId;
	private Long panelMemberId;

	public PanelPanelMemberId(Long panelId, Long panelMemberId) {
		this.panelId = panelId;
		this.panelMemberId = panelMemberId;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        PanelPanelMemberId that = (PanelPanelMemberId) o;
        return Objects.equals(panelId, that.panelId) &&
               Objects.equals(panelMemberId, that.panelMemberId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(panelId, panelMemberId);
    }
}
