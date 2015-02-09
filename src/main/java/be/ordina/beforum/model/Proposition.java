package be.ordina.beforum.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.web.util.HtmlUtils;

import be.ordina.beforum.helper.Helper;
import be.ordina.beforum.model.Role;
import lombok.Data;

@Data
public class Proposition {

	@Data
	public class UserSummary {
		private String id;
		private String firstName;
		private String lastName;
		private Role role;
	}
	
	public enum Status {
		NEW,
		AGENDA,
		DENIED,
		APPROVED,
		CROWDFUNDING,
		PLANNED,
		EXECUTING,
		EXECUTED
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
	private int votesDiff;
	private int votesTotal;
	private int comments;
	private List<String> tags;
	
	private Status status;
	private Date date;
	private int amount;
	private int amountPledged;
	
	public String getText() {
		return Helper.bbcode(HtmlUtils.htmlEscape(text));
	}
	public String getTitle() {
		return HtmlUtils.htmlEscape(title);
	}
}
