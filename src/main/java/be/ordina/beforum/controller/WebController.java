package be.ordina.beforum.controller;

import java.time.LocalDate;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import be.ordina.beforum.model.User;
import be.ordina.beforum.model.Vote;
import be.ordina.beforum.services.PropositionService;
import be.ordina.beforum.services.UserService;

@Controller
@Scope("session")
public class WebController {
    
	@Autowired
	private PropositionService propositions; 
	@Autowired
	private UserService users; 

	User currentUser=null;
	
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
    	if ((id.getDocumentType()!=DocumentType.BELGIAN_CITIZEN && id.getDocumentType()!=DocumentType.KIDS_CARD) ||
    			id.getCardValidityDateBegin().after(LocalDate.now()) ||
    			id.getCardValidityDateEnd().before(LocalDate.now())) {
    		model.addAttribute("id", id);
    		return "nobelgian";
    	}

    	Address address = (Address)session.getAttribute("eid.address");
    	byte[] photo = (byte[])session.getAttribute("eid.photo");
    	currentUser = users.logUser(id, address, photo);
    	session.setAttribute("authenticated_id", currentUser.get_id());
    	model.addAttribute("user", currentUser);
    	model.addAttribute("propositions", propositions.getByZip(address.getZip()));
    	return "home";
    }   

    @RequestMapping(value="/moreinfo/{userId}")
    public String info(HttpSession session, Model model,
    		@PathVariable("userId") String userId) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
    	User user = users.findUser(userId);
		model.addAttribute("author", user);
		model.addAttribute("user", currentUser);
    	return "info";
    }   

    @RequestMapping(value="/photo/{userId}")
    @ResponseBody
    public ResponseEntity<byte[]> photo(HttpSession session,
    		@PathVariable("userId") String userId) {
    	byte[] photo = (byte[])users.findUser(userId).getPhoto();    		
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.IMAGE_JPEG);
    	responseHeaders.set("Content-Disposition", "attachment");
    	return new ResponseEntity<byte[]>(photo, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/addproposition",method=RequestMethod.GET)
    public String addProposition(HttpSession session, Model model) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
		model.addAttribute("user", currentUser);
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
    	propositions.save((String)auth, id.getFirstName(), id.getName(),
    					  address.getZip(), title, text);
    	return identified(session, model);
    }   

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.GET)
    public String proposition(HttpSession session, Model model,
    		@PathVariable("propId") String propId) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model);
		model.addAttribute("user", currentUser);
    	model.addAttribute("proposition", propositions.get(propId));
    	Vote vote = propositions.getVote(propId, (String)auth);
    	int voteDir=0;
    	if (vote != null)
    		voteDir = vote.getDirection();
    	model.addAttribute("vote", voteDir);
    	return "proposition";
    }
    
    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="favor")
    public String voteFavor(HttpSession session, Model model,
    			@PathVariable("propId") String propId) {
    	propositions.registerVote ((String)session.getAttribute("authenticated_id"), propId, 1);
    	return proposition(session, model, propId);
    }

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="against")
    public String voteAgainst(HttpSession session, Model model,
    			@PathVariable("propId") String propId) {
    	propositions.registerVote ((String)session.getAttribute("authenticated_id"), propId, -1);
    	return proposition(session, model, propId);
    }

}
