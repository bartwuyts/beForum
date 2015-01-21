package be.ordina.beforum;

import org.apache.catalina.Context;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableAutoConfiguration
@EnableWebSecurity
@ComponentScan
public class Application implements EmbeddedServletContainerCustomizer {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void customize(final ConfigurableEmbeddedServletContainer container)
    {
        ((TomcatEmbeddedServletContainerFactory) container).addContextCustomizers(new TomcatContextCustomizer()
        {
            @Override
            public void customize(Context context)
            {
                context.setUseHttpOnly(false);
            }
        });
    }
}