package be.ordina.beforum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {
	    
	    @Override
	    protected String getDatabaseName() {
	        return "beforum";
	    }

	    @Override
	    public Mongo mongo() throws Exception {
	      return new Mongo();
	    }


	    @Override
	    protected String getMappingBasePackage() {
	        return "be.ordina.beforum.model";
	    }
}
