package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Tag;

public interface TagRepository extends PagingAndSortingRepository<Tag, String> {

	Tag findBy_id(String id);
	List<Tag> findAll();

}