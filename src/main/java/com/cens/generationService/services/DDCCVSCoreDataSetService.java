/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.services;

import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import java.math.BigDecimal;
import java.util.List;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import util.HapiFhirTools;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Service
public class DDCCVSCoreDataSetService {
    
    private static final  Logger log = LoggerFactory.getLogger(DDCCVSCoreDataSetService.class);
    
    
    /**
     * 
     * @param qr: QuestionnaireResponse con conjunto minimo de datos.
     * @return ObjectNode 
     */
    public String QRtoDDCCVSCoreDataSetStringJson(QuestionnaireResponse qr)
    {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("resourceType", "DDCCCoreDataSet");
        
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items = qr.getItem();
        ObjectNode certificate = JsonNodeFactory.instance.objectNode();
        node.putPOJO("certificate", certificate);
        ObjectNode certificatePeriod = JsonNodeFactory.instance.objectNode();
        certificate.putPOJO("period", certificatePeriod);
        ObjectNode vaccination = JsonNodeFactory.instance.objectNode();
        node.putPOJO("vaccination", vaccination);
        OperationOutcome out = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue;
       
        
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = findItem("hcid",items);
        if(item == null || item.getAnswerFirstRep().getValue()==null || item.getAnswerFirstRep().getValue().toString().isBlank()){
            addNotFoundIssue("hcid", out);
        }
        else{
            String stringFromItem = getStringFromItem("hcid", item, out);
            if(stringFromItem!=null){
                certificate.putRawValue("hcid",new RawValue("{\"value\":\""+stringFromItem+"\"}"));
                certificate.put("version","RC2");
            }
        }
        item = findItem("name",items);
        if(item == null || item.getAnswerFirstRep().getValue()==null || item.getAnswerFirstRep().getValue().toString().isBlank()){
            addNotFoundIssue("name", out);
        }
        else{
            String stringFromItem = getStringFromItem("name", item, out);
            if(stringFromItem!=null){
                node.put("name",stringFromItem);
            }
        }
        item = findItem("birthDate",items);
        if(item != null ){
            String stringFromItem = getStringFromItemDate("birthDate", item, out);
            if(stringFromItem!=null){
                node.put("birthDate",stringFromItem);
            }
        }
        item = findItem("identifier",items);
        if(item != null ){
            String stringFromItem = getStringFromItem("identifier", item, out);
            if(stringFromItem!=null){
                node.put("identifier",stringFromItem);
            }
        }
        item = findItem("pha",items);
        if(item == null || item.getAnswerFirstRep().getValue().toString().isBlank()){
            addNotFoundIssue("pha", out);
        }
        else{
            String stringFromItem = getStringFromItem("pha", item, out);
            if(stringFromItem!=null){
                certificate.putRawValue("issuer",new RawValue("{\"identifier\":{\"value\":\""+stringFromItem+"\"}}"));
            }
        }
        item = findItem("valid_from",items);
        if(item != null ){
            String stringFromItem = getStringFromItemDate("valid_from", item, out);
            if(stringFromItem!=null){
                certificatePeriod.put("start",item.getAnswerFirstRep().getValueDateType().getValueAsString());
            }
        }
        item = findItem("valid_until",items);
        if(item != null ){
            String stringFromItem = getStringFromItemDate("valid_until", item, out);
            if(stringFromItem!=null){
                certificatePeriod.put("end",item.getAnswerFirstRep().getValueDateType().getValueAsString());
            }
        }
        
        
        item = findItem("vaccine",items);
        if(item == null || item.getAnswerFirstRep().getValueCoding()==null){
            addNotFoundIssue("vaccine", out);
        }
        else{
            ObjectNode json = getValueCodingFromItemInJsonFormat("vaccine", item, out);
            if(json!=null){
                vaccination.putPOJO("vaccine", json);
            }
        }
        item = findItem("brand",items);
        if(item == null || item.getAnswerFirstRep().getValueCoding()==null){
            addNotFoundIssue("brand", out);
        }
        else{
            ObjectNode json = getValueCodingFromItemInJsonFormat("brand", item, out);
            if(json!=null){
                vaccination.putPOJO("brand", json);
            }
        }
        item = findItem("manufacturer",items);
        if(item != null ){
            ObjectNode json = getValueCodingFromItemInJsonFormat("manufacturer", item, out);
            if(json!=null){
                vaccination.putPOJO("manufacturer", json);
            }
        }
        item = findItem("ma_holder",items);
        if(item == null || item.getAnswerFirstRep().getValueCoding()==null){
            addNotFoundIssue("ma_holder", out);
        }
        else{
            ObjectNode json = getValueCodingFromItemInJsonFormat("ma_holder", item, out);
            if(json!=null){
                vaccination.putPOJO("maholder", json);
            }
        }    
        
        item = findItem("lot",items);
        if(item == null || item.getAnswerFirstRep().getValue().toString().isBlank()){
            addNotFoundIssue("lot", out);
        }
        else{
            String stringFromItem = getStringFromItem("lot", item, out);
            if(stringFromItem!=null){
                vaccination.put("lot",stringFromItem);
            }
        }
        
        
        item = findItem("date",items);
        if(item == null || item.getAnswerFirstRep().getValueDateType().isEmpty()){
            addNotFoundIssue("date", out);
        }
        else{
            String stringFromItem = getStringFromItemDate("date", item, out);
            if(stringFromItem!=null){
                vaccination.put("date",stringFromItem);
            }
        }
        
        item = findItem("vaccine_valid",items);
        if(item != null ){
            String stringFromItem = getStringFromItemDate("vaccine_valid", item, out);
            if(stringFromItem!=null){
                vaccination.put("valid_from",stringFromItem);
            }
        }
       
        item = findItem("dose",items);
        if(item == null || item.getAnswerFirstRep().getValue()==null || item.getAnswerFirstRep().getValue().toString().isBlank()){
            addNotFoundIssue("dose", out);
        }
        else{
            int num = getNumberFromItem("dose", item, out);
            if(num>0){
                vaccination.put("dose",num);
            }
            else{
                addInvalidIssue("dose", out);
            }
        }
        
        item = findItem("total_doses",items);
        if(item != null){
            int num = getNumberFromItem("total_doses", item, out);
            if(num>0){
                vaccination.put("totalDoses",num);
            }
            else{
                addInvalidIssue("total_doses", out);
            }
        }
          
        
        item = findItem("country",items);
        if(item == null || item.getAnswerFirstRep().getValueCoding()==null){
            addNotFoundIssue("country", out);
        }
        else{
            ObjectNode json = getValueCodingFromItemInJsonFormat("country", item, out);
            if(json!=null){
                vaccination.putPOJO("country", json);
            }
        } 
        
        item = findItem("centre",items);
        if(item == null || item.getAnswerFirstRep().getValue().toString().isBlank()){
            addNotFoundIssue("centre", out);
        }
        else{
            String stringFromItem = getStringFromItem("centre", item, out);
            if(stringFromItem!=null){
                vaccination.put("centre",stringFromItem);
            }
        }
          
        item = findItem("hw",items);
        if(item != null ){
            String stringFromItem = getStringFromItem("hw", item, out);
            if(stringFromItem!=null){
                certificate.putRawValue("practitioner",new RawValue("{\"value\":\""+stringFromItem+"\"}"));
            }
        }
            
        item = findItem("disease",items);
        if(item != null ){
            ObjectNode json = getValueCodingFromItemInJsonFormat("disease", item, out);
            if(json!=null){
                vaccination.putPOJO("disease", json);
            }
        }             
        
        item = findItem("due_date",items);
        if(item != null ){
            String stringFromItem = getStringFromItemDate("due_date", item, out);
            if(stringFromItem!=null){
                vaccination.put("nextDose",stringFromItem);
            }
        }  
         
        item = findItem("sex",items);
        if(item != null ){
            ObjectNode json = getValueCodingFromItemInJsonFormat("sex", item, out);
            if(json!=null){
                String code  = json.get("code").asText();
                node.put("sex",code);
            }
        }
            
        if(out.getIssue().size()>0){
            String resourceToString = HapiFhirTools.resourceToString(out, OperationOutcome.class);
            log.error(resourceToString);
            return resourceToString;
        }
        return node.toString();
    }
    /**
     * 
     * @param qr: QuestionnaireResponse con conjunto minimo de datos.
     * @return ObjectNode 
     */
    public ObjectNode QRtoDDCCVSCoreDataSet(QuestionnaireResponse qr)
    {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("resourceType", "DDCCCoreDataSet");
        
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items = qr.getItem();
        ObjectNode certificate = JsonNodeFactory.instance.objectNode();
        node.putPOJO("certificate", certificate);
        ObjectNode certificatePeriod = JsonNodeFactory.instance.objectNode();
        certificate.putPOJO("period", certificatePeriod);
        ObjectNode vaccination = JsonNodeFactory.instance.objectNode();
        node.putPOJO("vaccination", vaccination);
        OperationOutcome out = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue;
       

        items.forEach(item -> {
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
            else if(item.getLinkId().equals("valid_from")){
                //node.put("birthdate",item.getAnswerFirstRep().getValueDateType().getValueAsString());
                certificatePeriod.put("start",item.getAnswerFirstRep().getValueDateType().getValueAsString());
            }
            else if(item.getLinkId().equals("valid_until")){
                //node.put("birthdate",item.getAnswerFirstRep().getValueDateType().getValueAsString());
                certificatePeriod.put("end",item.getAnswerFirstRep().getValueDateType().getValueAsString());
            }
            else if(item.getLinkId().equals("vaccine")){
                vaccination.putRawValue("vaccine",new RawValue("{\"system\":\""+
                        item.getAnswerFirstRep().getValueCoding().getSystem()+"\",\"code\":\""+
                        item.getAnswerFirstRep().getValueCoding().getCode()+"\"}"));
            }
            else if(item.getLinkId().equals("brand")){
                vaccination.putRawValue("brand",new RawValue("{\"system\":\""+
                        item.getAnswerFirstRep().getValueCoding().getSystem()+"\",\"code\":\""+
                        item.getAnswerFirstRep().getValueCoding().getCode()+"\"}"));
            }
            else if(item.getLinkId().equals("manufacturer")){
                vaccination.putRawValue("manufacturer",new RawValue("{\"system\":\""+
                        item.getAnswerFirstRep().getValueCoding().getSystem()+"\",\"code\":\""+
                        item.getAnswerFirstRep().getValueCoding().getCode()+"\"}"));
            }
            else if(item.getLinkId().equals("ma_holder")){
                vaccination.putRawValue("maholder",new RawValue("{\"system\":\""+
                        item.getAnswerFirstRep().getValueCoding().getSystem()+"\",\"code\":\""+
                        item.getAnswerFirstRep().getValueCoding().getCode()+"\"}"));
            }
            else if(item.getLinkId().equals("lot")){
                vaccination.put("lot",item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("date")){
                vaccination.put("date",item.getAnswerFirstRep().getValueDateType().asStringValue());
            }
            else if(item.getLinkId().equals("vaccine_valid")){
                vaccination.put("valid_from",item.getAnswerFirstRep().getValueDateType().asStringValue());
            }
            else if(item.getLinkId().equals("dose")){
                vaccination.put("dose",item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("total_doses")){
                vaccination.put("totalDoses",item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("country")){
                vaccination.putRawValue("country",new RawValue("{\"system\":\""+
                        item.getAnswerFirstRep().getValueCoding().getSystem()+"\",\"code\":\""+
                        item.getAnswerFirstRep().getValueCoding().getCode()+"\"}"));
            }
            else if(item.getLinkId().equals("centre")){
                vaccination.put("centre",item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("hw")){
                vaccination.putRawValue("practitioner",new RawValue("{\"value\":\""+item.getAnswerFirstRep().getValue().toString()+"\"}"));
            }
            else if(item.getLinkId().equals("disease")){
                vaccination.putRawValue("disease",new RawValue("{\"system\":\""+
                        item.getAnswerFirstRep().getValueCoding().getSystem()+"\",\"code\":\""+
                        item.getAnswerFirstRep().getValueCoding().getCode()+"\"}"));
            }
            else if(item.getLinkId().equals("due_date")){
                vaccination.put("nextDose",item.getAnswerFirstRep().getValueDateType().asStringValue());
            }
            else if(item.getLinkId().equals("sex")){
                node.put("sex",item.getAnswerFirstRep().getValueCoding().getCode());
            }
        });
          
        return node;
    }
    
    QuestionnaireResponse.QuestionnaireResponseItemComponent findItem(String findValue,List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items)
    {
        for(QuestionnaireResponse.QuestionnaireResponseItemComponent item : items){
            if(item.getLinkId()!=null && item.getLinkId().equals(findValue)){
                return item;
            }
        }
        return null;
    }
    
    void addNotFoundIssue(String value, OperationOutcome out){
        OperationOutcome.OperationOutcomeIssueComponent issue;
        issue = new OperationOutcome.OperationOutcomeIssueComponent();
        issue.setCode(OperationOutcome.IssueType.NOTFOUND);
        issue.setDiagnostics(value+" not found or not have a value");
        out.getIssue().add(issue);
    }
    
    void addInvalidIssue(String value, OperationOutcome out){
        OperationOutcome.OperationOutcomeIssueComponent issue;
        issue = new OperationOutcome.OperationOutcomeIssueComponent();
        issue.setCode(OperationOutcome.IssueType.INVALID);
        issue.setDiagnostics(value+" is invalid");
        out.getIssue().add(issue);
    }
    
    String getStringFromItem(String value,QuestionnaireResponse.QuestionnaireResponseItemComponent item,OperationOutcome out){
        try{
            return item.getAnswerFirstRep().getValue().toString();
        }
        catch(Exception ex){
            
            OperationOutcome.OperationOutcomeIssueComponent issue;
            issue = new OperationOutcome.OperationOutcomeIssueComponent();
            issue.setCode(OperationOutcome.IssueType.INVALID);
            
            issue.setDiagnostics("Data format error in QuestionnaireResponse.item.linkId['"+value+"']");
            out.getIssue().add(issue);
            return null;
        }
    }
    String getStringFromItemDate(String value,QuestionnaireResponse.QuestionnaireResponseItemComponent item,OperationOutcome out){
        try{
            return item.getAnswerFirstRep().getValue().dateTimeValue().asStringValue();
        }
        catch(Exception ex){
            OperationOutcome.OperationOutcomeIssueComponent issue;
            issue = new OperationOutcome.OperationOutcomeIssueComponent();
            issue.setCode(OperationOutcome.IssueType.INVALID);
            issue.setDiagnostics("Data format error in QuestionnaireResponse.item.linkId['"+value+"']");
            out.getIssue().add(issue);
            return null;
        }
    }
    ObjectNode getValueCodingFromItemInJsonFormat(String value,QuestionnaireResponse.QuestionnaireResponseItemComponent item,OperationOutcome out){
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        try{
            Coding valueCoding = item.getAnswerFirstRep().getValueCoding();
            String code = valueCoding.getCode();
            String system = valueCoding.getCodeElement().asStringValue();
            if(code == null || system == null){
                addNotFoundIssue(value+"[code,system]", out);
                return null;
            }
            json.put("system", valueCoding.getSystem());
            json.put("code", valueCoding.getCodeElement().asStringValue());
        }
        catch(Exception ex){
            OperationOutcome.OperationOutcomeIssueComponent issue;
            issue = new OperationOutcome.OperationOutcomeIssueComponent();
            issue.setCode(OperationOutcome.IssueType.INVALID);
            issue.setDiagnostics("Data format error in QuestionnaireResponse.item.linkId['"+value+"']");
            out.getIssue().add(issue);
            return null;
        }
        return json;
    }
    
    
    int getNumberFromItem(String value,QuestionnaireResponse.QuestionnaireResponseItemComponent item,OperationOutcome out){
        try{
            int valueAsInteger = item.getAnswerFirstRep().getValueIntegerType().getValue().intValue();
            return valueAsInteger;
        }
        catch(Exception ex){
            
            OperationOutcome.OperationOutcomeIssueComponent issue;
            issue = new OperationOutcome.OperationOutcomeIssueComponent();
            issue.setCode(OperationOutcome.IssueType.INVALID);
            
            issue.setDiagnostics("Data format error in QuestionnaireResponse.item.linkId['"+value+"']");
            out.getIssue().add(issue);
            return 0;
        }
    }
    
}
