package be.ordina.beforum.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Zip;

public interface ZipRepository extends PagingAndSortingRepository<Zip, String> {

	Zip findBy_id(String id);
    Zip findByZipcode(String zipcode);

}