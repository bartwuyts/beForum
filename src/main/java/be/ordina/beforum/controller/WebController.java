package be.ordina.beforum.controller;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import be.fedict.eid.applet.service.DocumentType;
import be.fedict.eid.applet.service.Identity;
import be.fedict.eid.applet.service.Address;
import be.ordina.beforum.model.Comment;
import be.ordina.beforum.model.User;
import be.ordina.beforum.model.Vote;
import be.ordina.beforum.services.CommentService;
import be.ordina.beforum.services.PropositionService;
import be.ordina.beforum.services.TagService;
import be.ordina.beforum.services.UserService;

@Controller
@Scope("session")
public class WebController {
    
	@Autowired
	private PropositionService propositions; 
	@Autowired
	private UserService users; 
	@Autowired
	private TagService tags; 
	@Autowired
	private CommentService comments; 

	User currentUser=null;
	
    @RequestMapping(value="/")
    public String index(HttpSession session, Model model,
    		@RequestParam(value="tags", required=false) List<String>searchTags,
			@RequestParam(value="order", required=false) String order) {
    	System.err.println("in function 'index' - start");
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null) {
	    	model.addAttribute("sessionId", session.getId() );
	    	System.err.println("in function 'index' - go to login page");
	        return "login";
    	}
    	System.err.println("in function 'index' - let's generate some data");
    	Sort sorting=PropositionService.sortCreated;
    	if (order != null) {
    		if (order.equals("time")) {
    			sorting = PropositionService.sortCreated;
    		} else if (order.equals("popularity")) {
    			sorting = PropositionService.sortPopularity;
    		} else if (order.equals("controversial")) {
    			sorting = PropositionService.sortControversial;    		
    		}
    		model.addAttribute("order", order);
    	}
    	model.addAttribute("user", currentUser);
    	if (searchTags==null || searchTags.size()==0) {
    		model.addAttribute("propositions", propositions.getByZip(currentUser.getAddress().getZip(), sorting));
    	} else {
    		model.addAttribute("propositions", propositions.getByZipAndTags(currentUser.getAddress().getZip(), searchTags, sorting));    		
    	}
		model.addAttribute("tagList", tags.findAll());
    	System.err.println("in function 'index' - go to home page");
    	return "home";
    }

    @RequestMapping(value="/logout")
    public String logout(HttpSession session, Model model) {
    	session.removeAttribute("authenticated_id");
    	return "redirect:/";
    }   

    @RequestMapping(value="/identified")
    public String identified(HttpSession session, Model model,
    		@RequestParam(value="tags", required=false) List<String>searchTags) {
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	if (id==null) {
    		return index(session, model, null, null);
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
    	return "redirect:/";
    }   

    @RequestMapping(value="/moreinfo/{userId}")
    public String info(HttpSession session, Model model,
    		@PathVariable("userId") String userId) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model, null, null);
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
    		return index(session, model, null, null);
		model.addAttribute("user", currentUser);
		model.addAttribute("tagList", tags.findAll());
    	return "addproposition";
    }

    @RequestMapping(value="/addproposition",method=RequestMethod.POST)
    public String addProposition(HttpSession session, Model model,
    			@RequestParam(value="title") String title,
    			@RequestParam(value="text") String text,
    			@RequestParam(value="tags") List<String> tags) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return "redirect:/";
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	Address address = (Address)session.getAttribute("eid.address");
    	propositions.save((String)auth, id.getFirstName(), id.getName(), currentUser.getOfficial(),
    					  address.getZip(), title, text, tags);
    	return "redirect:/";
    }   

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.GET)
    public String proposition(HttpSession session, Model model,
    		@PathVariable("propId") String propId) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return index(session, model, null, null);
		model.addAttribute("user", currentUser);
    	model.addAttribute("proposition", propositions.get(propId));
    	Vote vote = propositions.getVote(propId, (String)auth);
    	int voteDir=0;
    	if (vote != null)
    		voteDir = vote.getDirection();
    	model.addAttribute("vote", voteDir);
    	model.addAttribute("comments", comments.find(propId));
    	return "proposition";
    }

    @RequestMapping(value="/comments/{top}/{parentId}",method=RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Comment>> comments(HttpSession session, Model model,
    		@PathVariable("top") int top,
    		@PathVariable("parentId") String parentId) {
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	responseHeaders.set("Content-Disposition", "attachment");
    	List<Comment> result = comments.find(parentId);
    	return new ResponseEntity<List<Comment>>(result, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/voteComment/{id}/{direction}",method=RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Integer> voteComment(HttpSession session, Model model,
    		@PathVariable("id") String id,
    		@PathVariable("direction") int direction) {
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	responseHeaders.set("Content-Disposition", "attachment");
    	int result = comments.registerVote ((String)session.getAttribute("authenticated_id"), id, direction);
    	return new ResponseEntity<Integer>(new Integer(result), responseHeaders, HttpStatus.OK);
    }
    
    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="favor")
    public String voteFavor(HttpSession session, Model model,
    			@PathVariable("propId") String propId) {
    	propositions.registerVote ((String)session.getAttribute("authenticated_id"), propId, 1);
    	return "redirect:/proposition/"+propId;
    }

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="against")
    public String voteAgainst(HttpSession session, Model model,
    			@PathVariable("propId") String propId) {
    	propositions.registerVote ((String)session.getAttribute("authenticated_id"), propId, -1);
    	return "redirect:/proposition/"+propId;
    }

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="comment")
    public String comment(HttpSession session, Model model,
    			@PathVariable("propId") String propId,
    			@RequestBody MultiValueMap<String,String> body) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return "redirect:/";

    	String comment = body.getFirst("comment_text");
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	comments.addComment((String)auth, id.getFirstName(), id.getName(), currentUser.getOfficial(), propId, true, comment);
    	return "redirect:/proposition/"+propId;
    }

    @RequestMapping(value="/subcomment/{propId}/{commentId}",method=RequestMethod.POST, params="comment")
    public String subcomment(HttpSession session, Model model,
			    @PathVariable("propId") String propId,
    			@PathVariable("commentId") String commentId,
    			@RequestBody MultiValueMap<String,String> body) {
    	Object auth=session.getAttribute("authenticated_id");
    	if (auth==null)
    		return "redirect:/";

    	String comment = body.getFirst("comment_text");
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	comments.addComment((String)auth, id.getFirstName(), id.getName(), currentUser.getOfficial(), commentId, false, comment);
    	return "redirect:/proposition/"+propId;
    }

}
