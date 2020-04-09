package com.ims.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TMAP_IMS_PANEL_CANDIDATE")
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Setter
@Getter
@NoArgsConstructor
@JsonPropertyOrder({ "candidateId", "candidateName", "email", "contactNo", "attendance", "status", "completeFeedback",
		"feedback", "panelId", "processId", "processSeq", "processName", "processStartDate", "processEndDate",
		"eventName" })
public class PanelCandidate implements Serializable {

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "panel", column = @Column(name = "PANEL_ID")),
			@AttributeOverride(name = "candidate", column = @Column(name = "CANDIDATE_ID")) })
	private PanelCandidateId id;

	// @Id
	@MapsId("panelId")
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "PANEL_ID", nullable = false)
	@JsonBackReference(value = "panel")
	private Panel panel;

	// @Id
	@MapsId("candidateId")
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "CANDIDATE_ID", nullable = false)
	@JsonBackReference(value = "candidate")
	private Candidate candidate;

	@Column(name = "ATTENDANCE")
	private Integer attendance = 0;

	@Column(name = "STATUS")
	private Integer status = 0;

	@Column(name = "FEEDBACK", length = 500)
	private String feedback;

	@Column(name = "COMPLETEFEEDBACK", length = 500)
	private int completeFeedback;

	public PanelCandidate(Candidate candidate, int attendance, int status, String feedback) {
		this.candidate = candidate;
		this.status = status;
		this.feedback = feedback;
		this.attendance = attendance;
	}

	public PanelCandidate(Candidate candidate) {
		this.candidate = candidate;
	}

	public PanelCandidate(Panel panel, Candidate candidate) {
		this.panel = panel;
		this.candidate = candidate;
	}

	public PanelCandidate(PanelCandidateId id, Panel panel, Candidate candidate) {
		this.id = id;
		this.panel = panel;
		this.candidate = candidate;
	}

	public Long getCandidateId() {
		return this.candidate.getCandidateId();
	}

	public String getCandidateName() {
		return this.candidate.getCandidateName();
	}

	public String getContactNo() {
		return this.candidate.getContactNo();
	}

	public String getEmail() {
		return this.candidate.getEmail();
	}

	public Long getPanelId() {
		return this.panel.getPanelId();
	}

	@JsonIgnore
	public String getPanelName() {
		return this.panel.getPanelName();
	}

	@JsonIgnore
	public Long getProcessId() {
		return this.panel.getProcess().getProcessId();
	}

	@JsonIgnore
	public int getProcessSeq() {
		return this.panel.getProcess().getProcessSeq();
	}

	public String getProcessName() {
		return this.panel.getProcess().getProcessName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PanelCandidate))
			return false;
		PanelCandidate that = (PanelCandidate) o;
		return Objects.equals(panel.getPanelName(), that.panel.getPanelName())
				&& Objects.equals((candidate).getCandidateName(), (that.candidate).getCandidateName())
				&& Objects.equals(attendance, that.attendance) && Objects.equals(status, that.status)
				&& Objects.equals(feedback, that.feedback);
	}

	@Override
	public int hashCode() {
		return Objects.hash(panel.getPanelName(), (candidate).getCandidateName(), attendance, status, feedback);
	}
}
