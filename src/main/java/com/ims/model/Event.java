package com.ims.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@Entity
@Table(name = "TMAS_IMS_EVENT")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder({ "eventId", "eventName", "startDate", "endDate", "processes", "candidates", "panelMembers", "createdId", "updatedId", "createdDate", "updatedDate"})
public class Event implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@JsonProperty("eventId")
	@Column(name = "EVENT_ID", nullable = false, length = 10)
	@NotNull(message = "Event id is required")
	private Long eventId;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Process> processes;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Candidate> candidates;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<PanelMember> panelMembers;

	//@JsonProperty("title")
	@JsonAlias({"title"})
	@Column(name = "EVENT_NAME", nullable = false, length = 100)
	@NotEmpty(message = "Event name is required")
	private String eventName;

	//@JsonProperty("hrId")
	@JsonAlias({"hrId"})
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

	// @JsonSerialize(using=JsonDateTimeSerializer.class)
	// @JsonDeserialize(using=JsonDateTimeDeserializer.class)

	// @JsonProperty(value = "openFrom", access = Access.WRITE_ONLY)
	// @DateTimeFormat(pattern="yyyy MMM dd HH:mm:ss z", iso = ISO.DATE_TIME)
	// @JsonSerialize(using=JsonDateTimeSerializer.class)
	// @JsonDeserialize(using=JsonDateTimeDeserializer.class)

	//@JsonProperty(value = "openFrom")
	@JsonAlias({"openFrom"})
	@JsonFormat(pattern = "yyyy MMM dd HH:mm:ss z", timezone = "IST")
	@Column(name = "START_DATE", length = 20)
	private LocalDateTime startDate;

	//@JsonProperty(value = "openTill")
	@JsonAlias({"openTill"})
	@JsonFormat(pattern = "yyyy MMM dd HH:mm:ss z", timezone = "IST")
	@Column(name = "END_DATE", length = 20)
	private LocalDateTime endDate;

	@PrePersist
	public void prePersist() {
		this.createdId = "Admin";
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedId = "Admin";
	}
}
