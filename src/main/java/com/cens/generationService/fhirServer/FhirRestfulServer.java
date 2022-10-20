package com.cens.generationService.fhirServer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.openapi.OpenApiInterceptor;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import com.cens.generationService.ApplicationProperties;
import com.cens.generationService.resourcesProviders.BundleResourceProvider;
import com.cens.generationService.resourcesProviders.QuestionnaireResponseResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;

//@Configuration
@Import(ApplicationProperties.class)
@Component 
//@WebServlet(urlPatterns = "/fhir/*", loadOnStartup = 1, displayName = "Gateway CO", asyncSupported = true)
public class FhirRestfulServer extends RestfulServer {
    
    private static final  Logger log = LoggerFactory.getLogger(FhirRestfulServer.class); 
    @Autowired
    ApplicationProperties properties;
    @Autowired
    BundleResourceProvider bundleResourceProvider;
    @Autowired
    QuestionnaireResponseResourceProvider questResResourceProvider;
 
    public FhirRestfulServer()  {
        super();
    }

    
    
    @Override
    protected void initialize() throws ServletException {
            // Create a context for the appropriate version
            super.initialize();
            
            
            setFhirContext(FhirContext.forR4());
            
            OpenApiInterceptor openApiInterceptor = new OpenApiInterceptor();
            registerInterceptor(openApiInterceptor);
            registerProvider(this.bundleResourceProvider);
            registerProvider(this.questResResourceProvider);
            // Format the responses in nice HTML
            registerInterceptor(new ResponseHighlighterInterceptor());

    }
    
    
}
