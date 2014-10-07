package be.ordina.beforum.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.Proposition;
import be.ordina.beforum.model.Vote;
import be.ordina.beforum.repository.PropositionRepository;
import be.ordina.beforum.repository.VoteRepository;

@Service
public class PropositionService {
    
	@Autowired
	private PropositionRepository propositions; 
	@Autowired
	private VoteRepository votes; 

	public PropositionService () {	
	}
	
    public void registerVote (String userId, String propId, int direction) {
    	Proposition prop = propositions.findBy_id(propId);
    	int votesFavor = prop.getVotesFavor();
    	int votesAgainst = prop.getVotesAgainst();
    	
    	Vote previousVote = votes.findByPropositionAndVoter(propId, userId);
    	if (previousVote != null) {
    		if (previousVote.getDirection() == direction)
    			return;
    		else {
    			
    		}
    		votes.delete(previousVote);
    	}
    	
    	if (direction > 0) {
    		votesFavor++;
    		if (previousVote != null)
    			votesAgainst--;
    	} else { 
    		votesAgainst++;
    		if (previousVote != null)
    			votesFavor--;
    	}
    	prop.setVotesFavor(votesFavor);
    	prop.setVotesAgainst(votesAgainst);
    	propositions.save(prop);
    	
    	Vote vote = new Vote();
    	vote.setProposition(propId);
    	vote.setDirection(direction);
    	vote.setVoter(userId);
    	vote.setWhen(new Date());
    	votes.save(vote);
    }

    public Proposition get(String propId) {
    	return propositions.findBy_id(propId);
    }

    public List<Proposition> getByZip(String zip) {
    	return propositions.findByZipcode(zip);
    }
    
    public Vote getVote(String propId, String auth) {
        return votes.findByPropositionAndVoter(propId, auth);    	
    }
    
    public Proposition save(String authorId, String authorFirstName, String authorName,
    				 String zip, String title, String text) {
    	Proposition prop=new Proposition();
    	Proposition.UserSummary creator = prop.new UserSummary();
    	creator.setId(authorId);
    	creator.setFirstName(authorFirstName);
    	creator.setLastName(authorName);
    	prop.setCreator(creator);
    	prop.setZipcode(zip);
    	prop.setTitle(title);
    	prop.setText(text);
    	return propositions.save(prop);
    }
}
