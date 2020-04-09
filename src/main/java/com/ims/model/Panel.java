package com.ims.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TMAS_IMS_PANEL")
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Data
@NoArgsConstructor
@JsonPropertyOrder({ "panelId", "panelName", "roomNo", "processId", "processName", "processStartDate",
		"processEndDate", "eventName", "panelCandidates", "panelPanelMembers", "assignedCnt", "selectedCnt", "rejectedCnt" })
public class Panel implements Serializable {

	@Id
	@Column(name = "PANEL_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long panelId;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROCESS_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_PANEL_PROCESS_ID"))
	private Process process;

	@JsonManagedReference(value = "panel")
	@OneToMany(mappedBy = "panel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<PanelCandidate> panelCandidates = new HashSet<PanelCandidate>(0);

	@JsonManagedReference(value = "panel")
	@OneToMany(mappedBy = "panel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<PanelPanelMember> panelPanelMembers = new HashSet<PanelPanelMember>(0);

	@Column(name = "PANEL_NAME", nullable = false, length = 40)
	private String panelName;

	@Column(name = "ROOM_NO", length = 10)
	private String roomNo;

	@JsonIgnore
	@Column(name = "CREATED_ID", length = 20)
	private String createdId;

	@JsonIgnore
	@Column(name = "CREATED_DATE", length = 20)
	@CreationTimestamp
	private LocalDateTime createdDate;

	@JsonIgnore
	@Column(name = "UPDATED_ID", length = 20)
	private String updatedId;

	@JsonIgnore
	@Column(name = "UPDATED_DATE", length = 20)
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	@PrePersist
	public void prePersist() {
		this.createdId = "Admin";
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedId = "Admin";
	}

	// getter method to retrieve the ProcessId
	public Long getProcessId() {
		return this.process.getProcessId();
	}

	public String getProcessName() {
		return this.process.getProcessName();
	}

	public String getProcessStartDate() {
		return this.process.getStartDate().toString();
	}

	public String getProcessEndDate() {
		return this.process.getEndDate().toString();
	}

	public String getEventName() {
		return this.process.getEvent().getEventName();
	}

	public Integer getAssignedCnt() {
		return this.panelCandidates.size();
	}

	public Integer getSelectedCnt() {
		return (int) this.panelCandidates.stream().filter(panelCandidate -> panelCandidate.getStatus() == 2).count();
	}

	public Integer getRejectedCnt() {
		return (int) this.panelCandidates.stream().filter(panelCandidate -> panelCandidate.getStatus() == 3).count();
	}

	public Panel(String panelName) {
		this.panelName = panelName;
	}

	public Panel(String panelName, String roomNo) {
		this.panelName = panelName;
		this.roomNo = roomNo;
	}

	public Panel(String panelName, PanelPanelMember[] panelPanelMembers, PanelCandidate... panelCandidates) {
		this.panelName = panelName;
		for (PanelCandidate panelCandidate : panelCandidates)
			panelCandidate.setPanel(this);
		this.panelCandidates = Stream.of(panelCandidates).collect(Collectors.toSet());

		for (PanelPanelMember panelPanelMember : panelPanelMembers)
			panelPanelMember.setPanel(this);
		this.panelPanelMembers = Stream.of(panelPanelMembers).collect(Collectors.toSet());
	}

	public void removePanelCandidate(PanelCandidate panelCandidate) {
		panelCandidate.setCandidate(null);
		this.panelCandidates.remove(panelCandidate);
	}

	public void removePanelCandidates(PanelCandidate... panelCandidates) {
		for (PanelCandidate panelCandidate : panelCandidates)
			panelCandidate.setCandidate(null);
		this.panelCandidates = Stream.of(panelCandidates).collect(Collectors.toSet());
	}

	public void removeAllPanelCandidate() {
		this.panelCandidates.forEach(panelCandidate -> panelCandidate.setCandidate(null));
		this.panelCandidates.clear();
	}

	public void addPanelCandidate(PanelCandidate panelCandidate) {
		this.panelCandidates.add(panelCandidate);
	}

	public void addPanelCandidates(PanelCandidate... panelCandidates) {

		for (PanelCandidate panelCandidate : panelCandidates)
			panelCandidate.setPanel(this);
		this.panelCandidates = Stream.of(panelCandidates).collect(Collectors.toSet());
	}

}
