package be.ordina.beforum.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import be.fedict.eid.applet.service.AppletServiceServlet;

@Configuration
public class WebConfig {
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
    	ServletRegistrationBean registration = new ServletRegistrationBean(new AppletServiceServlet(),"/applet-service");
    	Map<String,String> params = new HashMap<String,String>();
    	params.put("IncludeAddress", "true");
    	registration.setInitParameters(params);
    	return registration;
    }
    
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
         
        // keytool -genkey -alias tomcat -storetype PKCS12 -keyalg RSA -keysize 2048 -keystore keystore.p12 -validity 3650
        // keytool -list -v -keystore keystore.p12 -storetype pkcs12
         
        // curl -u user:password -k https://127.0.0.1:9000/greeting
         
        final String keystoreFile = "C:\\Users\\bawy\\Documents\\workspace-sts-3.5.1.RELEASE\\burgerForum\\keystore.p12";
        final String keystorePass = "burgerForum";
        final String keystoreType = "PKCS12";
        final String keystoreProvider = "SunJSSE";
        final String keystoreAlias = "tomcat";
     
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.addConnectorCustomizers((TomcatConnectorCustomizer) (Connector con) -> {
            con.setScheme("https");
            con.setSecure(true);
            Http11NioProtocol proto = (Http11NioProtocol) con.getProtocolHandler();
            proto.setSSLEnabled(true);
            proto.setKeystoreFile(keystoreFile);
            proto.setKeystorePass(keystorePass);
            proto.setKeystoreType(keystoreType);
            proto.setProperty("keystoreProvider", keystoreProvider);
            proto.setKeyAlias(keystoreAlias);
        });
     
         
        return factory;
    }
}
