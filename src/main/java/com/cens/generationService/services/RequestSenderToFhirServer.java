/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.cens.generationService.ApplicationProperties;
import javax.annotation.PostConstruct;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Service
public class RequestSenderToFhirServer {

    private static final  Logger log = LoggerFactory.getLogger(RequestSenderToFhirServer.class); 
    @Autowired
    ApplicationProperties properties;
    FhirContext ctx;
    IGenericClient serverClient;
    
    
    public RequestSenderToFhirServer() {
        ctx = FhirContext.forR4();  
    }
    
//@PostConstruct
    public void configServer(String baseUrl) {
        this.serverClient = ctx.newRestfulGenericClient(baseUrl);
        this.serverClient.getFhirContext().getRestfulClientFactory().setSocketTimeout(200*1000);
    }

    
    
}
