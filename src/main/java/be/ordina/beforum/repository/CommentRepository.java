package be.ordina.beforum.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import be.ordina.beforum.model.Comment;

public interface CommentRepository extends PagingAndSortingRepository<Comment, String> {

	Comment findBy_id(String id);
	List<Comment> findByParentId(String parentId, Sort order);

}