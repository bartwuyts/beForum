package be.ordina.beforum.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
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
import be.ordina.beforum.model.Proposition;
import be.ordina.beforum.model.Role;
import be.ordina.beforum.model.Tag;
import be.ordina.beforum.model.TagGroup;
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

	private static String defaultZip="9120";
	
    @RequestMapping(value="/")
    public String index(HttpSession session, Model model, AbstractAuthenticationToken principal,
    		@RequestParam(value="tag", required=false) String searchTag,
    		@RequestParam(value="status", required=false) String searchStatus,
    		@RequestParam(value="tags", required=false) List<String>searchTags,
			@RequestParam(value="order", required=false) String order) {
    	
    	Sort sorting=PropositionService.sortCreated;
    	if (order == null) {
    		order = "time";
    	}
		if (order.equals("time")) {
			sorting = PropositionService.sortCreated;
		} else if (order.equals("popularity")) {
			sorting = PropositionService.sortPopularity;
		} else if (order.equals("controversial")) {
			sorting = PropositionService.sortControversial;    		
		}
		model.addAttribute("order", order);
		
		String zipCode = defaultZip;
		if (principal != null)
		{
			User currentUser = (User)principal.getPrincipal();
			zipCode = currentUser.getAddress().getZip();
		}
    	if ((searchTags==null || searchTags.size()==0) &&
    			(searchTag!=null && !searchTag.equals(""))) {
    		searchTags = new ArrayList<String>();
    		searchTags.add(searchTag);
    	}
    	if (searchTags==null || searchTags.size()==0) {
    		if (searchStatus==null) {
    			model.addAttribute("propositions", propositions.getByZip(zipCode, sorting));
    		} else {
    			model.addAttribute("propositions", propositions.getByZipAndStatus(zipCode, searchStatus, sorting));    			
        		model.addAttribute("currentStatus",searchStatus);
    		}
    	} else {
    		if (searchStatus==null) {
    			model.addAttribute("propositions", propositions.getByZipAndTags(zipCode, searchTags, sorting));
    		} else {
    			model.addAttribute("propositions", propositions.getByZipAndTagsAndStatus(zipCode, searchTags, searchStatus, sorting));    			
        		model.addAttribute("currentStatus",searchStatus);
    		}
    		model.addAttribute("currentTag",searchTag);
    	}
    	List<Tag> tagList = tags.findAll();
		model.addAttribute("tagList", tagList);
		Map<String, String> tagMap = new HashMap<String, String>();
		for (Tag tag : tagList) {
			tagMap.put(tag.get_id(), tag.getText());
		}
		model.addAttribute("tagMap", tagMap);
		List<TagGroup> tagGroups = tags.findAllExtended();
		Map<String, Integer> tagGroupMap = new HashMap<String, Integer>();
		for (TagGroup tagGroup : tagGroups) {
			tagGroupMap.put(tagGroup.get_id(), tagGroup.getCount());
		}

		model.addAttribute("tagGroupMap", tagGroupMap);
    	return "home";
    }

    @RequestMapping(value="/login", method=RequestMethod.GET)
    public String login(HttpServletRequest currentRequest, HttpSession session, Model model,
    		@RequestParam(value="identified", required=false) String identified,
    		@RequestParam(value="logout", required=false) String logout) {
    	
    	if (logout != null)
    		return "redirect:/";
    	
    	if (identified == null)
        	return "login";

    	SavedRequest savedRequest = 
    		    new HttpSessionRequestCache().getRequest(currentRequest, null);
    	
    	Identity id = (Identity)session.getAttribute("eid.identity");
    	if (id==null) {
    		return "redirect:/login";
    	}
    	if ((id.getDocumentType()!=DocumentType.BELGIAN_CITIZEN && id.getDocumentType()!=DocumentType.KIDS_CARD) ||
    			id.getCardValidityDateBegin().after(LocalDate.now()) ||
    			id.getCardValidityDateEnd().before(LocalDate.now())) {
    		model.addAttribute("id", id);
    		return "nobelgian";
    	}

    	Address address = (Address)session.getAttribute("eid.address");
    	byte[] photo = (byte[])session.getAttribute("eid.photo");
    	User user = users.logUser(id, address, photo);
    	Authentication authentication = new PreAuthenticatedAuthenticationToken(user, null, null);
    	SecurityContextHolder.getContext().setAuthentication(authentication);
    	return "redirect:"+savedRequest.getRedirectUrl();
    }   

    @RequestMapping(value="/moreinfo/{userId}")
    public String info(HttpSession session, Model model, AbstractAuthenticationToken principal,
    		@PathVariable("userId") String userId) {
    	User user = users.findUser(userId);
		model.addAttribute("author", user);
    	return "info";
    }   

    @RequestMapping(value="/moreinfome/{userId}")
    public String infome(HttpSession session, Model model,
    		@PathVariable("userId") String userId) {
    	return "infome";
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
    public String addProposition(HttpSession session, Model model, AbstractAuthenticationToken principal) {
    	model.addAttribute("tagList", tags.findAll());
    	return "addproposition";
    }

    @RequestMapping(value="/addproposition",method=RequestMethod.POST)
    public String addProposition(HttpSession session, Model model, AbstractAuthenticationToken principal,
    			@RequestParam(value="title") String title,
    			@RequestParam(value="text") String text,
    			@RequestParam(value="tags") List<String> tags) {
    	User currentUser = (User)principal.getPrincipal();

    	User.Identity id = currentUser.getIdentity();
    	User.Address address = currentUser.getAddress();
    	propositions.save(currentUser.get_id(), id.getFirstName(), id.getName(), currentUser.getRole(),
    					  address.getZip(), title, text, tags);
    	return "redirect:/";
    }   

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.GET)
    public String proposition(HttpSession session, Model model, AbstractAuthenticationToken principal,
    		@PathVariable("propId") String propId) {
    	Vote vote = null;
    	if (principal != null) {
        	User currentUser = (User)principal.getPrincipal();
    		vote = propositions.getVote(propId, currentUser.get_id());
    	}
    	Proposition test = propositions.get(propId);
    	if (test.getStatus().equals(Proposition.Status.DENIED))
    		vote=null;
    	model.addAttribute("proposition", propositions.get(propId));
    	int voteDir=0;
    	if (vote != null)
    		voteDir = vote.getDirection();
    	model.addAttribute("vote", voteDir);
    	model.addAttribute("comments", comments.find(propId));
    	return "proposition";
    }

    @RequestMapping(value="/proposition/status/{propId}",method=RequestMethod.POST)
    public String updateProposition(HttpSession session, Model model, AbstractAuthenticationToken principal,
    		@PathVariable("propId") String propId,
			@RequestParam(value="status", required=false) String status,
			@RequestParam(value="date", required=false) @DateTimeFormat(pattern="dd/MM/yyyy") Date date,
			@RequestParam(value="amount", required=false) double amount) {
    	propositions.update(propId, status, date, amount);
    	return "redirect:/proposition/"+propId;
    }

    @RequestMapping(value="/comments/{parentId}",method=RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Comment>> comments(HttpSession session, Model model,
    		@PathVariable("parentId") String parentId) {
    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	responseHeaders.set("Content-Disposition", "attachment");
    	List<Comment> result = comments.find(parentId);
    	return new ResponseEntity<List<Comment>>(result, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/voteComment/{id}/{direction}",method=RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Integer> voteComment(HttpSession session, Model model, AbstractAuthenticationToken principal,
    		@PathVariable("id") String id,
    		@PathVariable("direction") int direction) {
    	User currentUser = (User)principal.getPrincipal();

    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	responseHeaders.set("Content-Disposition", "attachment");
    	int result = comments.registerVote (currentUser.get_id(), id, direction);
    	return new ResponseEntity<Integer>(new Integer(result), responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/voteProposal/{id}/{direction}",method=RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Integer>> voteProposal(HttpSession session, Model model, AbstractAuthenticationToken principal,
    		@PathVariable("id") String id,
    		@PathVariable("direction") int direction) {
    	User currentUser = (User)principal.getPrincipal();

    	HttpHeaders responseHeaders = new HttpHeaders();
    	responseHeaders.setContentType(MediaType.APPLICATION_JSON);
    	responseHeaders.set("Content-Disposition", "attachment");
    	List<Integer> result = propositions.registerVote (currentUser.get_id(), id, direction);
    	return new ResponseEntity<List<Integer>>(result, responseHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/proposition/{propId}",method=RequestMethod.POST, params="comment")
    public String comment(HttpSession session, Model model, AbstractAuthenticationToken principal,
    			@PathVariable("propId") String propId,
    			@RequestParam(value="official", required=false) boolean official,
    			@RequestBody MultiValueMap<String,String> body) {
    	User currentUser = (User)principal.getPrincipal();

    	String comment = body.getFirst("comment_text");
    	User.Identity id = currentUser.getIdentity();
    	Role role = null;
    	if (official)
    		role = currentUser.getRole();
    	comments.addComment(currentUser.get_id(), id.getFirstName(), id.getName(), role, propId, true, comment);
    	return "redirect:/proposition/"+propId;
    }

    @RequestMapping(value="/subcomment/{propId}/{commentId}",method=RequestMethod.POST, params="comment")
    public String subcomment(HttpSession session, Model model, AbstractAuthenticationToken principal,
			    @PathVariable("propId") String propId,
    			@PathVariable("commentId") String commentId,
    			@RequestParam(value="official", required=false) boolean official,
    			@RequestBody MultiValueMap<String,String> body) {
    	User currentUser = (User)principal.getPrincipal();

    	String comment = body.getFirst("comment_text");
    	User.Identity id = currentUser.getIdentity();
    	Role role = null;
    	if (official)
    		role = currentUser.getRole();
    	comments.addComment(currentUser.get_id(), id.getFirstName(), id.getName(), role, commentId, false, comment);
    	return "redirect:/proposition/"+propId;
    }

}
