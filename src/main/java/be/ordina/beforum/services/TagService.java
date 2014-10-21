package be.ordina.beforum.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.Tag;
import be.ordina.beforum.repository.TagRepository;

@Service
public class TagService {
    
	@Autowired
	private TagRepository tags; 

	public Tag addTag(String text) {
		Tag tag = new Tag();
		tag.setText(text);
		return tags.save(tag);
	}
	
	public Tag findTag(String tagId) {
		return tags.findBy_id(tagId);
	}
	
    public List<Tag> findAll() {
    	return tags.findAll();
    }

}
