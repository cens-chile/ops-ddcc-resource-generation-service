/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.services;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.cens.generationService.ApplicationProperties;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import util.HapiFhirTools;
import util.ProfileConstants;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Service
public class BundleService {
    
    private static final  Logger log = LoggerFactory.getLogger(BundleService.class);
    @Autowired
    RequestSenderToFhirServer request;
    @Autowired
    PatientService patService;
    @Autowired 
    ApplicationProperties properties;
    
    public Bundle readBundleSHER(Bundle entry){
        Bundle b = new Bundle();
        
        //Debo validar Bundle, se necesita servidor con profiles
        
        Bundle.BundleEntryComponent questEntry;
        questEntry = HapiFhirTools.findEntryByResourceClassAndRemove(entry, QuestionnaireResponse.class,"POST");
        QuestionnaireResponse quest = (QuestionnaireResponse) questEntry.getResource();
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items = quest.getItem();
        
        Patient pat = new Patient();
        Organization org = new Organization();
        Immunization imm = new Immunization();
        Extension brand = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCEventBrand");
        Extension maHolder = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVaccineMarketAuthorization");
        Extension country = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCCountryOfEvent");
        Extension vacValid = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVaccineValidFrom");
        for(QuestionnaireResponse.QuestionnaireResponseItemComponent item : items){
            if(item.getLinkId().equals("hcid")){
                Bundle.BundleLinkComponent linkFirstRep = b.getLinkFirstRep();
                linkFirstRep.setRelation("publication");
                linkFirstRep.setUrl("urn:uuid:"+item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("identifier")){
                pat.getIdentifierFirstRep().setValue(item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("name")){
                pat.getNameFirstRep().setText(item.getAnswerFirstRep().getValue().toString());
            }
            else if(item.getLinkId().equals("sex")){
                pat.setGender(Enumerations.AdministrativeGender.fromCode(item.getAnswerFirstRep().getValueCoding().getCode()));
            }
            else if(item.getLinkId().equals("birthDate")){
                pat.setBirthDate(item.getAnswerFirstRep().getValueDateType().getValue());
            } 
            else if(item.getLinkId().equals("pha")){
                org.setName(item.getAnswerFirstRep().getValue().toString());
            } 
            else if(item.getLinkId().equals("brand")){
                brand.setValue(item.getAnswerFirstRep().getValueCoding());
                imm.addExtension(brand);
            } 
            else if(item.getLinkId().equals("ma_holder")){
                maHolder.setValue(item.getAnswerFirstRep().getValueCoding());
                imm.addExtension(maHolder);
            } 
            else if(item.getLinkId().equals("manufacturer")){
                maHolder.setValue(item.getAnswerFirstRep().getValueCoding());
                imm.addExtension(maHolder);
            }
            else if(item.getLinkId().equals("country")){
                country.setValue(item.getAnswerFirstRep().getValueCoding().getCodeElement());
                imm.addExtension(country);
            }
            else if(item.getLinkId().equals("vaccine_valid")){
                vacValid.setValue(item.getAnswerFirstRep().getValueDateType());
                imm.addExtension(country);
            }
            else if(item.getLinkId().equals("vaccine")){
                imm.getVaccineCode().addCoding(item.getAnswerFirstRep().getValueCoding());
            }
            else if(item.getLinkId().equals("date")){
                
                DateTimeType valueDateTimeType = new DateTimeType(item.getAnswerFirstRep().getValueDateType().getValue());
                imm.setOccurrence(valueDateTimeType);
            }
            else if(item.getLinkId().equals("centre")){
                imm.getLocation().setDisplay(item.getAnswerFirstRep().getValue().toString());
            } 
        }
        HapiFhirTools.printResource(pat, Patient.class);
        HapiFhirTools.printResource(imm, Immunization.class);
        return b;
    }
}
