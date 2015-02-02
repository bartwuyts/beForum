package be.ordina.beforum.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.Comment;
import be.ordina.beforum.model.Proposition;
import be.ordina.beforum.model.Role;
import be.ordina.beforum.model.Vote;
import be.ordina.beforum.repository.CommentRepository;
import be.ordina.beforum.repository.PropositionRepository;
import be.ordina.beforum.repository.VoteRepository;

@Service
public class CommentService {
    
	@Autowired
	private CommentRepository comments; 
	@Autowired
	private PropositionRepository propositions; 
	@Autowired
	private VoteRepository votes; 

	public Comment addComment(String authorId, String authorFirstName, String authorName, Role authorRole,
							  String parentId, boolean toplevel, String text) {
		Comment comment = new Comment();
		comment.setParentId(parentId);
    	Comment.UserSummary creator = comment.new UserSummary();
    	creator.setId(authorId);
    	creator.setFirstName(authorFirstName);
    	creator.setLastName(authorName);
    	creator.setRole(authorRole);
		comment.setCreator(creator);
		comment.setCreated(new Date());
		comment.setToplevel(toplevel);
		comment.setText(text);
		Comment result = comments.save(comment);
		if (toplevel) {
			Proposition parent = propositions.findBy_id(parentId);
			parent.setComments(parent.getComments()+1);
			propositions.save(parent);
		} else {
			Comment parent = comments.findBy_id(parentId);
			parent.setChildComments(parent.getChildComments()+1);
			comments.save(parent);
		}
		return result;
	}
	
    public int registerVote (String userId, String commentId, int direction) {
    	Comment comment = comments.findBy_id(commentId);
    	int votesFavor = comment.getVotesFavor();
    	int votesAgainst = comment.getVotesAgainst();
    	int votesDiff = comment.getVotesDiff();
    	
    	Vote previousVote = votes.findByIdAndVoter(commentId, userId);
    	if (previousVote != null) {
    		if (previousVote.getDirection() == direction)
    			return votesFavor-votesAgainst;
    		else {
    			
    		}
    		votes.delete(previousVote);
    	}
    	
    	if (direction > 0) {
    		votesFavor++;
    		if (previousVote != null) {
    			votesAgainst--;
    			votesDiff+=2;
    		} else {
    			votesDiff++;
    		}
    	} else { 
    		votesAgainst++;
    		if (previousVote != null) {
    			votesFavor--;
    			votesDiff-=2;
    		} else {
    			votesDiff--;
    		}
    	}
    	comment.setVotesFavor(votesFavor);
    	comment.setVotesAgainst(votesAgainst);
    	comment.setVotesDiff(votesDiff);
    	comments.save(comment);
    	
    	Vote vote = new Vote();
    	vote.setId(commentId);
    	vote.setDirection(direction);
    	vote.setVoter(userId);
    	vote.setWhen(new Date());
    	votes.save(vote);
    	return votesFavor-votesAgainst;
    }

    public List<Comment> find(String parentId) {
    	return comments.findByParentId(parentId, new Sort(new Sort.Order(Sort.Direction.DESC, "votesDiff")));
    }

    public Comment findId(String id) {
    	return comments.findBy_id(id);
    }
}
