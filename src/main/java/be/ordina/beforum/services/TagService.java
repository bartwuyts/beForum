package be.ordina.beforum.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import be.ordina.beforum.model.Proposition;
import be.ordina.beforum.model.Tag;
import be.ordina.beforum.model.TagGroup;
import be.ordina.beforum.repository.PropositionRepository;
import be.ordina.beforum.repository.TagRepository;

@Service
public class TagService {

	@Autowired
	MongoTemplate mongoTemplate;
	
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

    public List<TagGroup> findAllExtended() {
    	TypedAggregation<Proposition> agg = Aggregation.newAggregation(Proposition.class,
    			Aggregation.unwind("tags"),
    			Aggregation.group("tags").
    				count().as("count")
    			);
    	return mongoTemplate.aggregate(agg, TagGroup.class).getMappedResults();
    }

}
