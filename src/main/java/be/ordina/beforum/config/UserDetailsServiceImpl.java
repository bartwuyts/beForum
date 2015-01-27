package be.ordina.beforum.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.User;
import be.ordina.beforum.services.UserService;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(final String email) 
		throws UsernameNotFoundException {
 
		User user = userService.findEmail(email);
		if (user==null)
			throw new UsernameNotFoundException(email);
		return user;
	}
	
	org.springframework.security.core.userdetails.User buildUserForAuthentication(User user, 
																				  List<GrantedAuthority> authorities) {
		return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
	}
	
}
