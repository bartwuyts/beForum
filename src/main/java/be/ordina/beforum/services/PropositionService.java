package be.ordina.beforum.services;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

	@Autowired
	private TagService tagService; 

	public static final Sort sortCreated = new Sort(new Sort.Order(Sort.Direction.DESC, "created"));
	public static final Sort sortPopularity = new Sort(new Sort.Order(Sort.Direction.DESC, "votesDiff"));
	public static final Sort sortControversial = new Sort(new Sort.Order(Sort.Direction.DESC, "votesTotal"), new Sort.Order(Sort.Direction.DESC, "comments"));
	
	public PropositionService () {	
	}
	
    public void registerVote (String userId, String propId, int direction) {
    	Proposition prop = propositions.findBy_id(propId);
    	int votesFavor = prop.getVotesFavor();
    	int votesAgainst = prop.getVotesAgainst();
    	int votesTotal = prop.getVotesTotal();
    	int votesDiff = prop.getVotesDiff();
    	
    	Vote previousVote = votes.findByIdAndVoter(propId, userId);
    	if (previousVote != null) {
    		if (previousVote.getDirection() == direction)
    			return;
    		else {
    			
    		}
    		votes.delete(previousVote);
    	}
    	else
    		votesTotal++;
    	
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
    	prop.setVotesFavor(votesFavor);
    	prop.setVotesAgainst(votesAgainst);
    	prop.setVotesDiff(votesDiff);
    	prop.setVotesTotal(votesTotal);
    	propositions.save(prop);
    	
    	Vote vote = new Vote();
    	vote.setId(propId);
    	vote.setDirection(direction);
    	vote.setVoter(userId);
    	vote.setWhen(new Date());
    	votes.save(vote);
    }

    public Proposition get(String propId) {
    	return propositions.findBy_id(propId);
    }

    public List<Proposition> getByZip(String zip, Sort order) {
    	return propositions.findByZipcode(zip, order);
    }

    public List<Proposition> getByZipAndTags(String zip, List<String> tags, Sort order) {
    	return propositions.findByZipcodeAndContainsTags(zip, tags, order);
    }

    public Vote getVote(String propId, String auth) {
        return votes.findByIdAndVoter(propId, auth);    	
    }
    
    public Proposition save(String authorId, String authorFirstName, String authorName,
    				 String zip, String title, String text, List<String> tags) {
    	Proposition prop=new Proposition();
    	Proposition.UserSummary creator = prop.new UserSummary();
    	creator.setId(authorId);
    	creator.setFirstName(authorFirstName);
    	creator.setLastName(authorName);
    	prop.setCreator(creator);
    	prop.setZipcode(zip);
    	prop.setCreated(new Date());
    	prop.setTitle(title);
    	prop.setText(text);
    	for (int i=0; i<tags.size(); i++) {
    		if (tags.get(i).startsWith("+")) {
    			tags.set(i, tags.get(i).substring(1));
    			if (tags.get(i).length()>0) {
    				tags.set(i, tagService.addTag(tags.get(i)).get_id());
    			}
    		}
    	}
    	tags = tags.stream().filter(tag -> tag.length()>0).collect(Collectors.toList());
    	prop.setTags(tags);
    	return propositions.save(prop);
    }
}
