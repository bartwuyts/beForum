package be.ordina.beforum.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
public class User implements UserDetails {

	@Id
	private String _id;
	private Identity identity;
	private Address address;
	private byte[] photo;
	private Date firstLogin;
	private Date lastLogin;
	private String mainZip;
	private String mainCity;
	private Role role;
	private String email;
	private String password;

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
	@Data
	public class Role {
		private String description;
		private boolean officialComment;
		private boolean officialProposal;
		private boolean townAdmin;
		private boolean generalAdmin;
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

	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> result = new ArrayList<>();
		if (getRole() == null)
			return result; // user is in old format
		
		if (getRole().isOfficialComment())
			result.add(new SimpleGrantedAuthority("ROLE_OFFICIAL_COMMENT"));
		if (getRole().isOfficialProposal())
			result.add(new SimpleGrantedAuthority("ROLE_OFFICIAL_PROPOSAL"));
		if (getRole().isTownAdmin())
			result.add(new SimpleGrantedAuthority("ROLE_TOWN_ADMIN"));
		if (getRole().isGeneralAdmin())
			result.add(new SimpleGrantedAuthority("ROLE_GENERAL_ADMIN"));
		return result;
    }

	@Override
    public String getUsername() {
	    return email;
    }

	@Override
    public boolean isAccountNonExpired() {
	    return true;
    }

	@Override
    public boolean isAccountNonLocked() {
	    return true;
    }

	@Override
    public boolean isCredentialsNonExpired() {
	    return true;
    }

	@Override
    public boolean isEnabled() {
	    return true;
    }

}
