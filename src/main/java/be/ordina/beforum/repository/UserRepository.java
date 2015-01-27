package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.User;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

	User findBy_id(String id);
	User findByEmail(String email);
    List<User> findByAddressZip(int zipcode);
    Page<User> findByAddressZip(int zipcode, Pageable pageable);
    User findByIdentityNationalNumber(String nationalNumber);

}