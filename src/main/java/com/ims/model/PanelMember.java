package com.ims.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.opencsv.bean.CsvBindByName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TMAS_IMS_PANEL_MEMBER", uniqueConstraints = {
		@UniqueConstraint(columnNames = "EMAIL", name = "UK_CANDIDTE_EMAIL") })
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Data
@NoArgsConstructor
public class PanelMember implements Serializable {

	@Id
	@Column(name = "PANELMEMBER_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long panelMemberId;

	@JsonManagedReference(value = "panelMember")
	@OneToMany(mappedBy = "panelMember", cascade = CascadeType.ALL)
	private Set<PanelPanelMember> panelPanelMember = new HashSet<PanelPanelMember>(0);
	
	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_PANEL_MEMBER_EVENT_ID"))
    private Event event;

	@Column(name = "PANELMEMBER_NAME", nullable = false, length = 40)
	@NotEmpty(message = "PanelMember Name is required")
	@CsvBindByName
	private String panelMemberName;

	@Column(name = "EMAIL", nullable = false, length = 40)
	@NotEmpty(message = "Email is required")
	@CsvBindByName
	private String email;

	@Column(name = "CONTACT_NO", nullable = false, length = 10)
	@NotEmpty(message = "Contact Number is required")
	@CsvBindByName
	private String contactNo;

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
	
	@PrePersist
	public void prePersist() {
		this.createdId = "Admin";
	}

	/*
	 * @PreUpdate public void preUpdate() { this.updatedId = "Admin"; }
	 */

	public PanelMember(String panelMemberName) {
		this.panelMemberName = panelMemberName;
	}
}
