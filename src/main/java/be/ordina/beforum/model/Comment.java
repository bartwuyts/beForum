package be.ordina.beforum.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.web.util.HtmlUtils;

import be.ordina.beforum.helper.Helper;
import lombok.Data;

@Data
public class Comment {

	@Data
	public class UserSummary {
		private String id;
		private String firstName;
		private String lastName;
	}

	@Id
	private String _id;
	private Date created;
	private UserSummary creator;
	private boolean toplevel;
	private String parentId;
	private String text;
	private int votesFavor;
	private int votesAgainst;
	private int votesDiff;
	private int childComments;
	
	public String getText() {
		return Helper.bbcode(HtmlUtils.htmlEscape(text));
	}
}
