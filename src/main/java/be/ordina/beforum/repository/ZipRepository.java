package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Zip;

public interface ZipRepository extends PagingAndSortingRepository<Zip, String> {

	Zip findBy_id(String id);
    Zip findByZipcode(String zipcode);

}