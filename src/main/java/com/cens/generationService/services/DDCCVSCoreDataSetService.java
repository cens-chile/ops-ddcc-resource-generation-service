/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.services;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import java.math.BigDecimal;
import java.util.List;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.springframework.stereotype.Service;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Service
public class DDCCVSCoreDataSetService {
    
    
    public ObjectNode QRtoDDCCVSCoreDataSet(QuestionnaireResponse qr)
    {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("resourceType", "DDCCCoreDataSet");
        
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items = qr.getItem();
        ObjectNode certificate = JsonNodeFactory.instance.objectNode();
        node.putPOJO("certificate", certificate);
        for(QuestionnaireResponse.QuestionnaireResponseItemComponent item : items){
            if(item.getLinkId().equals("hcid")){
                certificate.putRawValue("hcid",new RawValue("{\"value\":\""+item.getAnswerFirstRep().getValue().toString()+"\"}"));
                certificate.put("version","RC2");
            }
            else if(item.getLinkId().equals("name")){
                node.put("name",item.getAnswerFirstRep().getValue().toString());
            }  
            else if(item.getLinkId().equals("birthDate")){
                node.put("birthdate",item.getAnswerFirstRep().getValueDateType().getValueAsString());
            }
            else if(item.getLinkId().equals("identifier")){
                node.putRawValue("identifier",new RawValue("{\"value\":\""+item.getAnswerFirstRep().getValue().toString()+"\"}"));
            }
            else if(item.getLinkId().equals("pha")){
                certificate.putRawValue("issuer",new RawValue("{\"identifier\":{\"value\":\""+item.getAnswerFirstRep().getValue().toString()+"\"}}"));
            }
            
        }
        
        return node;
    }
    
}
