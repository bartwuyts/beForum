package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Proposition;

public interface PropositionRepository extends PagingAndSortingRepository<Proposition, String> {

	Proposition findBy_id(String id);
    List<Proposition> findByZipcode(String zipcode, Sort sortable);
    Page<Proposition> findByZipcode(String zipcode, Pageable pageable);
    List<Proposition> findByCreatorId(String creatorId);

    @Query(value="{ $and : [{ 'zipcode' : ?0 }, {tags : { $all : ?1 } }] }")
    List<Proposition> findByZipcodeAndContainsTags(String zipcode, List<String> tags, Sort sortable);

}