/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.resourcesProviders;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.cens.generationService.ApplicationProperties;
import com.cens.generationService.services.DDCCVSCoreDataSetService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.StructureMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        
        log.info("Entry StructureMap/$transform request.");
        QuestionnaireResponse b = (QuestionnaireResponse) theRequestDetails.getResource();
        
        Map<String, String[]> requestParams = theServletRequest.getParameterMap();
        String[] source = requestParams.get("source");
        if (source == null || source.length <= 0) {
            throw new InvalidRequestException("source parameter not found.");
        }
        //String collect = theServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        //System.out.println("collect = " + collect);
        
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String res = "";
        
        if(source[0].equals("http://worldhealthorganization.github.io/ddcc/StructureMap/QRespToVSCoreDataSet")){
            //node = service.QRtoDDCCVSCoreDataSet(b);
            res = this.service.QRtoDDCCVSCoreDataSetStringJson(b);
        }
        else{
            throw new UnprocessableEntityException("Map not available with canonical url "+source[0]);
        }
        out.print(res);
        out.flush(); 
  
    }
    
   
}
