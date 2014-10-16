package be.ordina.beforum.model;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Tag {

	@Id
	private String _id;
	private String text;

	public String getId() {
		// This is needed to get the select2 UI control working
		return _id;
	}
}
