package be.ordina.beforum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {
	    
	    @Override
	    protected String getDatabaseName() {
	        return "beforum";
	    }

	    @Override
	    public Mongo mongo() throws Exception {
	      return new MongoClient(new MongoClientURI("mongodb://beforum:beforum@ds031329.mongolab.com:31329/beforum"));
	    }


	    @Override
	    protected String getMappingBasePackage() {
	        return "be.ordina.beforum.model";
	    }
}
