package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Proposition;

public interface PropositionRepository extends PagingAndSortingRepository<Proposition, String> {

	Proposition findBy_id(String id);
    List<Proposition> findByZipcode(int zipcode);
    Page<Proposition> findByZipcode(int zipcode, Pageable pageable);
    List<Proposition> findByCreator(String creator);

}