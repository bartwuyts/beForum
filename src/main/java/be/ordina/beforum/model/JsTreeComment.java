package be.ordina.beforum.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class JsTreeComment {

	@Data
	public class State {
		private boolean opened;
		private boolean disabled;
		private boolean selected;
	}
	private String id;
	private String text;
	private String state;
	private boolean children;
	private boolean icon=false;
	//private String icon;
	//private State state;
	//private List<JsTreeComment> children; 
	
	public JsTreeComment(Comment comment) {
		this.id = comment.get_id();
		this.text = comment.getText();
		this.state = "closed";
		this.children = true;
		//this.state = new State();
		//this.state.opened=false;
		//this.state.disabled=false;
		//this.state.selected=false;
		//this.children = new ArrayList<>();
	}
}
