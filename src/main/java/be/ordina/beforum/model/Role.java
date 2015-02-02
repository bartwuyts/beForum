package be.ordina.beforum.model;

import lombok.Data;

@Data
public class Role {
	private String description;
	private boolean officialComment;
	private boolean officialProposal;
	private boolean townAdmin;
	private boolean generalAdmin;
}
