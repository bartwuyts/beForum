package be.ordina.beforum.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.Proposition;
import be.ordina.beforum.model.Role;
import be.ordina.beforum.model.Vote;
import be.ordina.beforum.repository.PropositionRepository;
import be.ordina.beforum.repository.VoteRepository;

@Service
public class PropositionService {
    
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	private PropositionRepository propositions; 
	@Autowired
	private VoteRepository votes; 

	@Autowired
	private TagService tagService; 

	private static final String votesDiff="votesDiff";
	private static final String votesTotal="votesTotal";
	
	public static final Sort sortCreated = new Sort(new Sort.Order(Sort.Direction.DESC, "created"));
	public static final Sort sortPopularity = new Sort(new Sort.Order(Sort.Direction.DESC, votesDiff));
	public static final Sort sortControversial = new Sort(new Sort.Order(Sort.Direction.DESC, votesTotal), new Sort.Order(Sort.Direction.DESC, "comments"));
	
	public PropositionService () {	
	}
	
    public List<Integer> registerVote (String userId, String propId, int direction) {
    	Proposition prop = propositions.findBy_id(propId);
    	int votesFavor = prop.getVotesFavor();
    	int votesAgainst = prop.getVotesAgainst();
    	int votesTotal = prop.getVotesTotal();
    	int votesDiff = prop.getVotesDiff();
    	
    	Vote previousVote = votes.findByIdAndVoter(propId, userId);
    	if (previousVote != null) {
    		if (previousVote.getDirection() == direction)
    			return Arrays.asList(votesFavor, votesAgainst);
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
		return Arrays.asList(votesFavor, votesAgainst);
    }

    public Proposition get(String propId) {
    	return propositions.findBy_id(propId);
    }

    public List<Proposition> getByZip(String zip, Sort order) {
    	TypedAggregation<Proposition> agg = Aggregation.newAggregation(Proposition.class,
    			Aggregation.project("created","creator","text","title","votesFavor","votesAgainst","votesDiff","comments","tags")
    				.andExpression("votesFavor + votesAgainst").as(votesTotal)
    				.andExpression("votesFavor - votesAgainst").as(votesDiff),
    			Aggregation.sort(order)
    			);
    	return mongoTemplate.aggregate(agg, Proposition.class).getMappedResults();
    }

    public List<Proposition> getByZipAndTags(String zip, List<String> tags, Sort order) {
    	TypedAggregation<Proposition> agg = Aggregation.newAggregation(Proposition.class,
    			Aggregation.match(Criteria.where("tags").all(tags)),
    			Aggregation.project("created","creator","text","title","votesFavor","votesAgainst",
    								"votesDiff","comments","tags")
    				.andExpression("votesFavor + votesAgainst").as(votesTotal)
    				.andExpression("votesFavor - votesAgainst").as(votesDiff),
    			Aggregation.sort(order)
    			);
    	return mongoTemplate.aggregate(agg, Proposition.class).getMappedResults();
    }

    public Vote getVote(String propId, String auth) {
        return votes.findByIdAndVoter(propId, auth);    	
    }
    
    public Proposition save(String authorId, String authorFirstName, String authorName, Role authorRole,
    				 String zip, String title, String text, List<String> tags) {
    	Proposition prop=new Proposition();
    	Proposition.UserSummary creator = prop.new UserSummary();
    	creator.setId(authorId);
    	creator.setFirstName(authorFirstName);
    	creator.setLastName(authorName);
    	creator.setRole(authorRole);
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
    
    public Proposition update(String propId, String status, Date date, double amount) {
    	Proposition prop = propositions.findBy_id(propId);
    	switch(status) {
    		case "NEW": 
    			prop.setStatus(Proposition.Status.NEW);
    			break;
    		case "AGENDA": 
    			prop.setStatus(Proposition.Status.AGENDA);
    			break;
    		case "DENIED": 
    			prop.setStatus(Proposition.Status.DENIED);
    			break;
    		case "APPROVED": 
    			prop.setStatus(Proposition.Status.APPROVED);
    			break;
    		case "CROWDFUNDING": 
    			prop.setStatus(Proposition.Status.CROWDFUNDING);
    			break;
    		case "PLANNED": 
    			prop.setStatus(Proposition.Status.PLANNED);
    			break;
    		case "EXECUTING": 
    			prop.setStatus(Proposition.Status.EXECUTING);
    			break;
    		case "EXECUTED": 
    			prop.setStatus(Proposition.Status.EXECUTED);
    			break;
    		default: 
    			return null;
    	}
    	prop.setDate(date);
    	prop.setAmount(amount);
    	return propositions.save(prop);
    }
}
