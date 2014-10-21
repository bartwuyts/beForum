package be.ordina.beforum.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Proposition {

	@Data
	public class UserSummary {
		private String id;
		private String firstName;
		private String lastName;
	}
	
	@Id
	private String _id;
	private String zipcode;
	private Date created;
	private UserSummary creator;
	private String text;
	private String title;
	private int votesFavor;
	private int votesAgainst;
	private int comments;
	private List<String> tags;
}
