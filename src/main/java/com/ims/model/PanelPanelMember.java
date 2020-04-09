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
@Table(name = "TMAP_IMS_PANEL_PANELMEMBER")
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Setter @Getter @NoArgsConstructor
@JsonPropertyOrder({ "panelId", "panelName", "panelMemberId", "panelMemberName", "publicLink"})
public class PanelPanelMember implements Serializable {

	@EmbeddedId
	@AttributeOverrides({@AttributeOverride(name = "panel", column = @Column(name = "PANEL_ID") ),
	    @AttributeOverride(name = "panelMember", column = @Column(name = "PANELMEMBER_ID") )})
	private PanelPanelMemberId id;
	
	//@Id
	@MapsId("panelId")
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "PANEL_ID", nullable = false)
    @JsonBackReference(value="panel")
    private Panel panel;
	
	//@Id
	@MapsId("panelMemberId")
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "PANELMEMBER_ID", nullable = false)
    @JsonBackReference(value="panelMember")
    private PanelMember panelMember;
	
	@Column(name = "PUBLIC_LINK", length = 50)
	private String publicLink;
	
	@Column(name = "NOTIFY")
	private Integer notify = 0;
	
	public PanelPanelMember(PanelMember panelMember, String publicLink) {
        this.panelMember = panelMember;
        this.publicLink = publicLink;
    }
	
	@Column(name = "STATUS")
	private Integer status = 0;
	
	public PanelPanelMember(PanelMember panelMember) {
        this.panelMember = panelMember;
    }
	
	public PanelPanelMember(PanelPanelMemberId id, Panel panel, PanelMember panelMember) {
        this.id = id;
        this.panel = panel;
        this.panelMember = panelMember;
    }
	
	public PanelPanelMember(Panel panel, PanelMember panelMember) {
		this.panel = panel;
        this.panelMember = panelMember;
    }
	
	@JsonIgnore
	public Long getPanelId(){ return this.panel.getPanelId(); }

	@JsonIgnore
	public String getPanelName(){ return this.panel.getPanelName(); }
	
	public Long getPanelMemberId(){ return this.panelMember.getPanelMemberId(); }
	
	public String getPanelMemberName(){ return this.panelMember.getPanelMemberName(); }
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PanelPanelMember)) return false;
        PanelPanelMember that = (PanelPanelMember) o;
        return Objects.equals(panel.getPanelName(), that.panel.getPanelName()) &&
                Objects.equals((panelMember).getPanelMemberName(), (that.panelMember).getPanelMemberName()) &&
                Objects.equals(publicLink, that.publicLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(panel.getPanelName(), (panelMember).getPanelMemberName(), publicLink);
    }
}
