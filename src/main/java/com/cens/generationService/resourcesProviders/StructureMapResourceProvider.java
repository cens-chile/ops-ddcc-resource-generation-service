/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.resourcesProviders;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import com.cens.generationService.ApplicationProperties;
import com.cens.generationService.services.BundleService;
import com.cens.generationService.services.DDCCVSCoreDataSetService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.dstu2.model.StructureDefinition;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.StructureMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import util.HapiFhirTools;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Component
public class StructureMapResourceProvider implements IResourceProvider{

    private static final  Logger log = LoggerFactory.getLogger(StructureMapResourceProvider.class);
    @Autowired
    ApplicationProperties properties;
    @Autowired
    DDCCVSCoreDataSetService service;
    
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return StructureMap.class;
    }
    
    @Operation(name = "$transform", type = StructureMap.class, manualResponse = true)
    public void manualInputAndOutput(RequestDetails theRequestDetails,HttpServletRequest theServletRequest, HttpServletResponse response)
      throws IOException {
        QuestionnaireResponse b = (QuestionnaireResponse) theRequestDetails.getResource();
        HapiFhirTools.printResource(b, QuestionnaireResponse.class);
        String contentType = theServletRequest.getContentType();
        System.out.println("contentType = " + contentType);
        //String collect = theServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        //System.out.println("collect = " + collect);
        
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ObjectNode node = service.QRtoDDCCVSCoreDataSet(b);
        String toPrettyString = node.toPrettyString();
        System.out.println("toPrettyString = " + toPrettyString);
        out.print(node.toString());
        out.flush(); 
  
    }
    
   
}