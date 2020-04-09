package com.ims.bo;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PanelBO {

	/*
	 * @NotNull(message = "Attendance value is required", groups =
	 * AttendanceInfo.class) Integer attendance;
	 */
	
	@NotNull(message = "Room Number is required", groups = PanelInfo.class)
	String roomNo;
	
	@NotEmpty(message = "Panel Name is required", groups = PanelInfo.class)
	String panelName;
	
	@NotEmpty(message = "Candidate Id can't be null / Atleast one Candidate Id is required", groups = {PanelInfo.class, FeedbackInfo.class, AddMoreCandidateInfo.class})
	List<Long> candidateIds;
	
	@NotNull(message = "Present Candidate Id can't be null. Atleast empty array is expected.", groups = {AttendanceInfo.class})
	List<Long> presentCandidateIds;

	@NotNull(message = "Absent Candidate Id can't be null. Atleast empty array is expected.", groups = {AttendanceInfo.class})
	List<Long> absentCandidateIds;
	
	List<Long> panelMemberIds;
	
	@NotNull(message = "Panel count is required", groups = AutoAssignmentInfo.class)
	Integer panelCnt;
	
	@NotNull(message = "Panel Member count is required", groups = AutoAssignmentInfo.class)
	Integer panelMemberCntPerPanel;
	
	@NotNull(message = "Candidate count is required", groups = AutoAssignmentInfo.class)
	Integer candidateCntPerPanel;
	
	@NotNull(message = "Status value is required", groups = FeedbackInfo.class)
	Integer status;
	
	//@NotEmpty(message = "Feedback is required", groups = FeedbackInfo.class)
	String feedback;
	
}
