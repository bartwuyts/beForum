package be.ordina.beforum.model;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class TagGroup {

	@Id
	private String _id;
	private int count;
}
