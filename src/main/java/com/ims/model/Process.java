package com.ims.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Entity
@Table(name = "TMAS_IMS_PROCESS", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"EVENT_ID", "PROCESS_SEQ"}, name = "UK_PROCESS_SEQ") })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Data
@JsonPropertyOrder({ "processId", "processName", "processSeq", "startDate", "endDate", "createdId", "createdDate",
		"updatedId", "updatedDate" })
public class Process implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "PROCESS_ID", nullable = false, length = 10)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long processId;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EVENT_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_PROCESS_EVENT_ID"))
	private Event event;

	@JsonManagedReference
	@OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Panel> panels;

	@Column(name = "PROCESS_NAME", nullable = false, length = 40)
	@NotEmpty(message = "Process Name is required")
	private String processName;

	/*
	 * @Column(name = "AUTO_ASSIGN", nullable = false) private int autoAssign;
	 */

	@Column(name = "PROCESS_SEQ", nullable = false)
	@NotNull(message = "Process sequence is required")
	private int processSeq;
	
	@Column(name = "PARENT_ID", nullable = false, length = 5)
	private int parentId;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "START_DATE", length = 20)
	private LocalDateTime startDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	@Column(name = "END_DATE", length = 20)
	private LocalDateTime endDate;

	@Column(name = "CREATED_ID", length = 20)
	private String createdId;

	@Column(name = "UPDATED_ID", length = 20)
	private String updatedId;

	@Column(name = "CREATED_DATE", length = 20)
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "UPDATED_DATE", length = 20)
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	@JsonIgnore
	public List<PanelCandidate> getPanelCandidates() {
		List<PanelCandidate> panelCandidates = new ArrayList<PanelCandidate>();
		if (this.panels != null)
			this.panels.forEach(
					panel -> panel.getPanelCandidates().stream().filter(panelCandidate -> panelCandidate != null)
							.forEach(panelCandidate -> panelCandidates.add(panelCandidate)));
		return panelCandidates;
	}

	@JsonIgnore
	public List<PanelPanelMember> getPanelPanelMembers() {
		List<PanelPanelMember> panelPanelMembers = new ArrayList<PanelPanelMember>();
		if (this.panels != null)
			this.panels.forEach(
					panel -> panel.getPanelPanelMembers().stream().filter(panelPanelMember -> panelPanelMember != null)
							.forEach(panelPanelMember -> panelPanelMembers.add(panelPanelMember)));
		return panelPanelMembers;
	}

	@PrePersist
	public void prePersist() {
		this.createdId = "Admin";
	}

	@PreUpdate
	public void preUpdate() {
		this.createdId = "Admin";
	}
}