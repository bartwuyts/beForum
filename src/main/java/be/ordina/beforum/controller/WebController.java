package be.ordina.beforum.controller;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import be.fedict.eid.applet.service.DocumentType;
import be.fedict.eid.applet.service.Identity;
import be.fedict.eid.applet.service.Address;
import be.ordina.beforum.model.Proposition;
import be.ordina.beforum.model.User;
import be.ordina.beforum.model.Vote;
import be.ordina.beforum.repository.PropositionRepository;
import be.ordina.beforum.repository.UserRepository;
import be.ordina.beforum.repository.VoteRepository;

@Controller
@Scope("session")
public class WebController {
    
	@Autowired
	private PropositionRepository propositions; 
	@Autowired
	private UserRepository users; 
	@Autowired
	private VoteRepository votes; 
	
    @RequestMapping(value="/")
    public String index(HttpSession session, Model model) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth!=null)
    		return identified(session, model);
    	model.addAttribute("sessionId", session.getId() );
        return "login";
    }

    @RequestMapping(value="/logout")
    public String logout(HttpSession session, Model model) {
    	session.removeAttribute("authenticated_id");
    	return index(session, model);
    }   

    @RequestMapping(value="/identified")
    public String identified(HttpSession session, Model model) {
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	if (id==null) {
    		return index(session, model);
    	}
    	if (id.getDocumentType()!=DocumentType.BELGIAN_CITIZEN ||
    			id.getCardValidityDateBegin().after(LocalDate.now()) ||
    			id.getCardValidityDateEnd().before(LocalDate.now())) {
    		model.addAttribute("nationality", id.getNationality());
    		return "nobelgian";
    	}

    	Address address = (Address)session.getAttribute("eid.address");
    	User user = logUser(id, address);
    	session.setAttribute("authenticated_id", user.get_id());
    	model.addAttribute("identity", id);
    	model.addAttribute("address", address);
    	model.addAttribute("user", user);
    	model.addAttribute("propositions", propositions.findByZipcode(Integer.parseInt(address.getZip())));
    	List<Proposition> test = propositions.findByZipcode(Integer.parseInt(address.getZip()));
    	return "home";
    }   

    @RequestMapping(value="/moreinfo")
    public String info(HttpSession session, Model model) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	Address address = (Address)session.getAttribute("eid.address");
    	model.addAttribute("identity", id);
    	model.addAttribute("address", address);
    	return "info";
    }   

    @RequestMapping(value="/addproposition",method=RequestMethod.GET)
    public String addProposition(HttpSession session, Model model) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	Address address = (Address)session.getAttribute("eid.address");
    	model.addAttribute("identity", id);
    	model.addAttribute("address", address);
    	return "addproposition";
    }

    @RequestMapping(value="/addproposition",method=RequestMethod.POST)
    public String addProposition(HttpSession session, Model model,
    			@RequestParam(value="title") String title,
    			@RequestParam(value="text") String text) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	Address address = (Address)session.getAttribute("eid.address");
    	Proposition prop=new Proposition();
    	Proposition.UserSummary creator = prop.new UserSummary();
    	creator.setId((String)auth);
    	creator.setFirstName(id.getFirstName());
    	creator.setLastName(id.getName());
    	prop.setCreator(creator);
    	prop.setZipcode(Integer.parseInt(address.getZip()));
    	prop.setTitle(title);
    	prop.setText(text);
    	propositions.save(prop);
    	return identified(session, model);
    }   

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.GET)
    public String proposition(HttpSession session, Model model,
    		@PathVariable("propId") String propId) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	Address address = (Address)session.getAttribute("eid.address");
    	model.addAttribute("identity", id);
    	model.addAttribute("address", address);
    	model.addAttribute("proposition", propositions.findBy_id(propId));
    	Vote vote = votes.findByPropositionAndVoter(propId, (String)auth);
    	int voteDir=0;
    	if (vote != null)
    		voteDir = vote.getDirection();
    	model.addAttribute("vote", voteDir);
    	return "proposition";
    }
    
    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="favor")
    public String voteFavor(HttpSession session, Model model,
    			@PathVariable("propId") String propId) {
    	registerVote ((String)session.getAttribute("authenticated_id"), propId, 1);
    	return proposition(session, model, propId);
    }

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="against")
    public String voteAgainst(HttpSession session, Model model,
    			@PathVariable("propId") String propId) {
    	registerVote ((String)session.getAttribute("authenticated_id"), propId, -1);
    	return proposition(session, model, propId);
    }

    private void registerVote (String userId, String propId, int direction) {
    	Proposition prop = propositions.findBy_id(propId);
    	int voteCount = prop.getVotes();
    	
    	Vote previousVote = votes.findByPropositionAndVoter(propId, userId);
    	if (previousVote != null) {
    		if (previousVote.getDirection() == direction)
    			return;
    		else 
    			direction *= 2;
    		votes.delete(previousVote);
    	}
    	
    	voteCount += direction;
    	prop.setVotes(voteCount);
    	propositions.save(prop);
    	
    	Vote vote = new Vote();
    	vote.setProposition(propId);
    	vote.setDirection(direction);
    	vote.setVoter(userId);
    	vote.setWhen(new Date());
    	votes.save(vote);
    }

	private User logUser(Identity id, Address address) {
		User user = users.findByIdentityNationalNumber(id.getNationalNumber());
		if (user == null) {
			user = addUser(id, address);
		}
		user.setLastLogin(new Date());
		users.save(user);
		return user;
	}
	
	private User addUser(Identity id, Address address) {
		User user = new User();
		user.fromEID(id,  address);
		user.setFirstLogin(new Date());
		return users.save(user);
	}

}
