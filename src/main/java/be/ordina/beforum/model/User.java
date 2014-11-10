package be.ordina.beforum.model;

import java.util.Date;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class User {

	@Id
	private String _id;
	private Identity identity;
	private Address address;
	private byte[] photo;
	private Date firstLogin;
	private Date lastLogin;
	private String mainZip;
	private String mainCity;
	private String official;

	public enum Gender {
		MALE,
		FEMALE
	}
	
	@Data
	public class Identity {
		private String cardNumber;
		private String chipNumber;
		private Date cardValidityDateBegin;
		private Date cardValidityDateEnd;
		private String cardDeliveryMunicipality;
		private String nationalNumber;
		private String name;
		private String firstName;
		private String middleName;
		private String nationality;
		private String placeOfBirth;
		private Date dateOfBirth;
		private Gender gender;
		private String nobleCondition;
		private String duplicate;
	}
	@Data
	public class Address {
		private String streetAndNumber;
		private String zip;
		private String city;
	}
		
	public void fromEID(be.fedict.eid.applet.service.Identity id, 
						be.fedict.eid.applet.service.Address addr,
						byte[] photo) {
		identity = new Identity();
		identity.setCardNumber(id.getCardNumber());
		identity.setChipNumber(id.getChipNumber());
		identity.setCardValidityDateBegin(id.getCardValidityDateBegin().getTime());
		identity.setCardValidityDateEnd(id.getCardValidityDateEnd().getTime());
		identity.setCardDeliveryMunicipality(id.getCardDeliveryMunicipality());
		identity.setNationalNumber(id.getNationalNumber());
		identity.setName(id.getName());
		identity.setFirstName(id.getFirstName());
		identity.setMiddleName(id.getMiddleName());
		identity.setNationality(id.getNationality());
		identity.setPlaceOfBirth(id.getPlaceOfBirth());
		identity.setDateOfBirth(id.getDateOfBirth().getTime());
		if (id.getGender()==be.fedict.eid.applet.service.Gender.MALE)
			identity.setGender(Gender.MALE);
		else
			identity.setGender(Gender.FEMALE);
		identity.setNobleCondition(id.getNobleCondition());
		identity.setDuplicate(id.getDuplicate());
		address = new Address();
		address.setStreetAndNumber(addr.getStreetAndNumber());
		address.setZip(addr.getZip());
		address.setCity(addr.getMunicipality());
		setPhoto(photo);
	}

}
