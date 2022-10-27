/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.resourcesProviders;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;


import ca.uhn.fhir.rest.server.IResourceProvider;
import com.cens.generationService.ApplicationProperties;
import com.cens.generationService.fhirServer.FhirRestfulServer;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Component
public class QuestionnaireResponseResourceProvider implements IResourceProvider{

    private static final  Logger log = LoggerFactory.getLogger(FhirRestfulServer.class);
    @Autowired
    ApplicationProperties properties;
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return QuestionnaireResponse.class;
    }
    
    /**
     *
     * @param quest
     * @return
     */
    @Operation(name="$QRtoCoreVSDataSet")
    public Bundle generateHealthCertificate(@ResourceParam QuestionnaireResponse quest) {
   
        System.out.println("quest = " + quest.getId());
        Bundle retVal = new Bundle();
        // Populate bundle with matching resources
        return retVal;
    }
    
    
}
