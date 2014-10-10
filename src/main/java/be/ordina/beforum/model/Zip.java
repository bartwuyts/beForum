package be.ordina.beforum.model;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Zip {

	@Id
	private String _id;
	private String zipcode;
	private String town;
	private String mainZipcode;
	private String mainTown;
	private String Province;

}
