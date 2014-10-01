package be.ordina.beforum.model;

import java.util.Date;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Vote {

	@Id
	private String _id;
	private String voter; // _id
	private int direction;
	private String proposition; // _id
	private Date when;

}
