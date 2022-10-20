/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.resourcesProviders;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.cens.generationService.ApplicationProperties;
import com.cens.generationService.fhirServer.FhirRestfulServer;
import com.cens.generationService.services.BundleService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.HapiFhirTools;
import static util.ProfileConstants.BUNDLE_SHER_PROFILE;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Component
public class BundleResourceProvider implements IResourceProvider{

    private static final  Logger log = LoggerFactory.getLogger(FhirRestfulServer.class);
    @Autowired
    ApplicationProperties properties;
    @Autowired
    BundleService service;
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Bundle.class;
    }
    @Read()
   public Bundle read(@IdParam IdType theId) {
      Bundle retVal = new Bundle();
      if (retVal == null) {
         throw new ResourceNotFoundException(theId);
      }
      return retVal;
   }
   
    @Transaction
    public Bundle transaction(@TransactionParam Bundle input, RequestDetails req) {
        
        Bundle retVal = new Bundle();
        Bundle.BundleType type = input.getType();
        System.out.println("type = " + type.toCode());
        String profile = HapiFhirTools.getProfile(input.getMeta());
        if(profile!=null && profile.equals(BUNDLE_SHER_PROFILE) && type.toCode().equals("batch")){
           service.readBundleSHER(input);
        }
        else 
            throw new PreconditionFailedException("Unknown Bundle Profile or Bundle.type");
        /*String username = req.getHeader("X-Consumer-Username");
        log.info("Username:"+ username);
        String profile = HapiFhirTools.getProfile(theInput.getMeta());
        log.info("Validando Bundle Profile");
        if(profile!=null && profile.equals(BUNDLE_CO_PROFILE)){
            retVal = service.readingBundleCo(theInput,req);
        }
        else 
            throw new PreconditionFailedException("Unknown Bundle Profile");
        
        retVal.setId(properties.getNacional());
        */
        return retVal;
    }
    
   
}
