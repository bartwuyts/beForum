package be.ordina.beforum.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.Comment;
import be.ordina.beforum.repository.CommentRepository;

@Service
public class CommentService {
    
	@Autowired
	private CommentRepository comments; 

	public Comment addComment(String authorId, String authorFirstName, String authorName,
							  String parentId, boolean toplevel, String text) {
		Comment comment = new Comment();
		comment.setParentId(parentId);
    	Comment.UserSummary creator = comment.new UserSummary();
    	creator.setId(authorId);
    	creator.setFirstName(authorFirstName);
    	creator.setLastName(authorName);
		comment.setCreator(creator);
		comment.setCreated(new Date());
		comment.setToplevel(toplevel);
		comment.setText(text);
		return comments.save(comment);
	}
	
    public List<Comment> find(String parentId, boolean toplevel) {
    	return comments.findByParentIdAndToplevel(parentId, toplevel);
    }

}
