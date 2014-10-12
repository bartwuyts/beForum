package be.ordina.beforum.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.fedict.eid.applet.service.Identity;
import be.fedict.eid.applet.service.Address;
import be.ordina.beforum.model.User;
import be.ordina.beforum.model.Zip;
import be.ordina.beforum.repository.UserRepository;
import be.ordina.beforum.repository.ZipRepository;

@Service
public class UserService {
    
	@Autowired
	private UserRepository users; 
	@Autowired
	private ZipRepository zipcodes; 

	public User logUser(Identity id, Address address, byte[] photo) {
		User user = users.findByIdentityNationalNumber(id.getNationalNumber());
		if (user == null) {
			user = addUser(id, address, photo);
		}
		user.setLastLogin(new Date());
		users.save(user);
		return user;
	}
	
	public User addUser(Identity id, Address address, byte[] photo) {
		User user = new User();
		user.fromEID(id, address, photo);
		user.setFirstLogin(new Date());
		Zip zipInfo = zipcodes.findByZipcode(address.getZip());
		user.setMainZip(zipInfo.getMainZipcode());
		user.setMainCity(zipInfo.getMainTown());
		return users.save(user);
	}
	
	public User findUser(String userId) {
		return users.findBy_id(userId);
	}

}
