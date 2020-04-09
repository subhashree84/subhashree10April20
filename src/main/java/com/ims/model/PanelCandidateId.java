package com.ims.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Data @NoArgsConstructor
@Setter @Getter @NoArgsConstructor
@Embeddable
@JsonIgnoreType
public class PanelCandidateId implements Serializable{
	private Long panelId;
	private Long candidateId;

	public PanelCandidateId(Long panelId, Long candidateId) {
		this.panelId = panelId;
		this.candidateId = candidateId;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        PanelCandidateId that = (PanelCandidateId) o;
        return Objects.equals(panelId, that.panelId) &&
               Objects.equals(candidateId, that.candidateId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(panelId, candidateId);
    }
}
