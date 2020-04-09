package com.ims.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TMAS_IMS_CANDIDATE", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"EVENT_ID", "EMAIL"}, name = "UK_CANDIDTE_EMAIL") })
		//@UniqueConstraint(columnNames = {"EVENT_ID", "CONTACT_NO"}, name = "UK_CANDIDTE_CONTACT_NO")})
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Data
@NoArgsConstructor
@JsonPropertyOrder({ "candidateId", "candidateName", "gender", "dob", "contactNo", "email", "address", "totalMarks",
		"score", "attemptStatus", "status", "parentId", "reportUrl", "createdId", "updatedId", "createdDate",
		"updatedDate" })
public class Candidate implements Serializable {

	@Id
	@Column(name = "CANDIDATE_ID", nullable = false, length = 10)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long candidateId;

	@JsonManagedReference(value = "candidate")
	@OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
	private Set<PanelCandidate> panelCandidates = new HashSet<PanelCandidate>(0);

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EVENT_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CANDIDATE_EVENT_ID"))
	// @JsonIgnoreProperties(value = {"candidate"})
	private Event event;

	@JsonAlias({ "name" })
	@Column(name = "CANDIDATE_NAME", nullable = false, length = 40)
	@NotEmpty(message = "Candidate Name is required")
	private String candidateName;

	@Column(name = "GENDER", length = 6)
	//@NotEmpty(message = "Gender is required")
	private String gender;

	@DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "DOB", length = 20)
	//@NotEmpty(message = "Date of Birth is required")
	private Date dob;

	@Column(name = "EMAIL", nullable = false, length = 40)
	@NotEmpty(message = "Email is required")
	private String email;

	@Column(name = "CONTACT_NO", length = 15)
	//@NotEmpty(message = "Contact Number is required")
	private String contactNo;

	@Column(name = "TOTAL_MARKS", length = 10)
	private Long totalMarks;

	@Column(name = "SCORE", length = 10)
	private Long score;

	@Column(name = "REPORT_URL", length = 250)
	private String reportUrl;

	@Column(name = "ATTEMPT_STATUS", length = 50)
	private String attemptStatus;

	@Column(name = "ADDRESS", length = 500)
	private String address;

	@Column(name = "STATUS", length = 5)
	private int status;

	@Column(name = "PARENT_ID", nullable = false, length = 5)
	private int parentId;

	@Column(name = "CREATED_ID", length = 20)
	private String createdId;

	@Column(name = "CREATED_DATE", length = 20)
	@CreationTimestamp
	private LocalDateTime createdDate;

	@Column(name = "UPDATED_ID", length = 20)
	private String updatedId;

	@Column(name = "UPDATED_DATE", length = 20)
	@UpdateTimestamp
	private LocalDateTime updatedDate;

	/*
	 * @Column(name = "PANEL_ASSIGNED", nullable = false) private int panelAssigned;
	 */

	@PrePersist
	public void prePersist() {
		this.createdId = "Admin";
	}

	/*
	 * @PreUpdate public void preUpdate() { this.updatedId = "Admin"; }
	 */

	public Candidate(String candidateName) {
		this.candidateName = candidateName;
	}

	public void addChild(PanelCandidate... panelCandidates) {

		for (PanelCandidate panelCandidate : panelCandidates)
			panelCandidate.setCandidate(this);
		this.panelCandidates = Stream.of(panelCandidates).collect(Collectors.toSet());
	}
}
