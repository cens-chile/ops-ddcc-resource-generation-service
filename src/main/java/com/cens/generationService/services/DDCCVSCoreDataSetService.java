/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.services;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;
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
                vaccination.put("validFrom",stringFromItem);
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
                vaccination.putRawValue("practitioner",new RawValue("{\"value\":\""+stringFromItem+"\"}"));
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
            String resourceToString = HapiFhirTools.resourceToString(out);
            log.error(resourceToString);
            return resourceToString;
        }
        return node.toString();
    }
    
    
    
    public String transformDDCCVSCoreDataSetToAddBundle(String core){
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        
        JsonNode node;
        try {
            node = mapper.readTree(core);
            String toString = node.toString();
            
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(DDCCVSCoreDataSetService.class.getName()).log(Level.SEVERE, null, ex);
            throw new FHIRException(ex.getMessage());
        }
        
        Bundle b = new Bundle();
        b.setType(Bundle.BundleType.TRANSACTION);
        Patient pat = new Patient();
        HapiFhirTools.addProfileToResource(pat,"http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCPatient");
        IdType patId = IdType.newRandomUuid();
        pat.setId(patId.getValue().split(":")[2]);
        Reference patRef = new Reference(patId);
        
        Organization org = new Organization();
        HapiFhirTools.addProfileToResource(org,"http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCOrganization");
        IdType orgId = IdType.newRandomUuid();
        org.setId(orgId.getValue().split(":")[2]);
        Reference orgRef = new Reference(orgId);
        
        Composition comp = new Composition();
        HapiFhirTools.addProfileToResource(comp,"http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVSComposition");
        IdType compId = IdType.newRandomUuid();
        comp.setId(compId.getValue().split(":")[2]);
        Reference compRef = new Reference(compId);
        
        
        Immunization imm = new Immunization();
        HapiFhirTools.addProfileToResource(imm,"http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCImmunization");
        IdType immId = IdType.newRandomUuid();
        imm.setId(immId.getValue().split(":")[2]);
        Reference immRef = new Reference(immId);
        imm.setStatus(ImmunizationStatus.COMPLETED);
         
        ImmunizationRecommendation immR = new ImmunizationRecommendation();
        HapiFhirTools.addProfileToResource(immR,"http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCImmunizationRecommendation");
        IdType immRId = IdType.newRandomUuid();
        immR.setId(immRId.getValue().split(":")[2]);
        Reference immRRef = new Reference(immRId);
        immR.getRecommendationFirstRep().getForecastStatus().getCodingFirstRep().setSystem("http://terminology.hl7.org/CodeSystem/immunization-recommendation-status");
        immR.getRecommendationFirstRep().getForecastStatus().getCodingFirstRep().setCode("due");
        immR.getRecommendationFirstRep().getDateCriterionFirstRep().getCode().getCodingFirstRep().setSystem("http://loinc.org");
        immR.getRecommendationFirstRep().getDateCriterionFirstRep().getCode().getCodingFirstRep().setCode("30980-7");
        immR.getRecommendationFirstRep().getSupportingImmunization().add(immRef);

        DocumentReference docR = new DocumentReference();
        String qrSystem = "http://worldhealthorganization.github.io/ddcc/CodeSystem/DDCC-QR-Format-CodeSystem";
        HapiFhirTools.addProfileToResource(docR,"http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCDocumentReferenceQR");
        IdType docRId = IdType.newRandomUuid();
        docR.setId(docRId.getValue().split(":")[2]);
        Reference docRRef = new Reference(docRId);
        docR.setStatus(DocumentReferenceStatus.CURRENT);
        docR.setType(new CodeableConcept(new Coding("http://worldhealthorganization.github.io/ddcc/CodeSystem/DDCC-QR-Type-CodeSystem"
                ,"who","WHO DDCC")));
        docR.setSubject(patRef);
        Attachment attachment = new Attachment();
        attachment.setContentType("attachment");
        docR.getContent().add(new DocumentReference.DocumentReferenceContentComponent(attachment)
        .setFormat(new Coding(qrSystem,"serialized",null)));
        docR.getContent().add(new DocumentReference.DocumentReferenceContentComponent(attachment)
        .setFormat(new Coding(qrSystem,"image",null)));
        docR.getContent().add(new DocumentReference.DocumentReferenceContentComponent(attachment)
        .setFormat(new Coding(qrSystem,"pdf",null)));
        //docR.getCategory().add(new CodeableConcept(
        //new Coding("http://worldhealthorganization.github.io/ddcc/CodeSystem/DDCC-QR-Category-Usage-CodeSystem",
        //"who", null)));
        
        docR.setDescription("WHO QR code for COVID 19 Vaccine Certificate");
        
        
        imm.setPatient(patRef);
        immR.setPatient(patRef);
        
        //QuestionnaireResponse debe ir vacio
        OperationOutcome out = new OperationOutcome();
        
        JsonNode get = node.get("name");
        if(get!=null){
            pat.getNameFirstRep().setUse(HumanName.NameUse.OFFICIAL);
            pat.getNameFirstRep().setText(get.asText());
        }
        else{
            addNotFoundIssue("name", out);
        }
        get = node.get("birthDate");
        if(get!=null)
            pat.setBirthDateElement(new DateType(get.asText()));
        
        get = node.get("identifier");
        if(get!=null){
            pat.getIdentifierFirstRep().setValue(get.asText());
        }
        
        get = node.get("sex");
        if(get!=null)
            pat.getGenderElement().setValueAsString(get.asText());
        
        
        
        
        JsonNode certificate = node.get("certificate");
        if(certificate==null){
            addNotFoundIssue("certificate", out);
        }
        try{
            JsonNode period = certificate.get("period");
            if(period!=null){
                JsonNode start = period.get("start");
                JsonNode end = period.get("end");
                if(start!=null){
                    Date readValue = df.parse(start.asText());
                    comp.getEventFirstRep().getPeriod().setStart(readValue, TemporalPrecisionEnum.DAY);
                }  
                if(end!=null){
                    Date readValue = df.parse(end.asText());
                    comp.getEventFirstRep().getPeriod().setStart(readValue, TemporalPrecisionEnum.DAY);
                }  
            }
            JsonNode issuer = certificate.get("issuer");
            if(issuer!=null){
                String pha = issuer.get("identifier").get("value").asText();
                Reference authority = imm.getProtocolAppliedFirstRep().getAuthority();
                authority.setReference(orgRef.getReference());
                authority.getIdentifier().setValue(pha);
                org.setName(pha);
                comp.getAuthorFirstRep().setReference(orgRef.getReference());
                comp.getAttesterFirstRep().setParty(orgRef);
                comp.getAttesterFirstRep().setMode(Composition.CompositionAttestationMode.OFFICIAL);
                docR.getAuthenticator().setReference(orgRef.getReference());
                
            }
            else{
                addNotFoundIssue("certificate.issuer", out);
            }
            
        }catch(Exception ex){
            addErrorIssue("certificate", ex.getMessage(), out);
        }
        
        JsonNode vaccination = node.get("vaccination");
        if(vaccination==null){
            addNotFoundIssue("vaccination", out);
        }
        try{
            
            JsonNode prac = vaccination.get("practitioner");
            if(prac!=null){
                String hw = prac.get("value").asText();
                Reference actor = imm.getPerformerFirstRep().getActor();
                actor.setType("Practitioner");
                actor.getIdentifier().setValue(hw);
            }
            JsonNode vaccine = vaccination.get("vaccine");
            if(vaccine!=null){
                String vS = vaccine.get("system").asText();
                String vC = vaccine.get("code").asText();
                CodeableConcept vaccineCodeC = imm.getVaccineCode();
                Coding coding = new Coding(vS, vC,"");
                vaccineCodeC.addCoding(coding);
                immR.getRecommendationFirstRep().getVaccineCodeFirstRep().addCoding(coding);
                
            }
            else{
                addNotFoundIssue("vaccination.vaccine", out);
            }
            
            JsonNode brand = vaccination.get("brand");
            if(brand!=null){
                Extension bra = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCEventBrand");
                String vS = brand.get("system").asText();
                String vC = brand.get("code").asText();
                bra.setValue(new Coding(vS, vC,""));
                imm.addExtension(bra);
            }
            else{
                addNotFoundIssue("vaccination.brand", out);
            }
            
            JsonNode manufacturer = vaccination.get("manufacturer");
            if(manufacturer!=null){
                String vS = manufacturer.get("system").asText();
                String vC = manufacturer.get("code").asText();
                Identifier identifier = imm.getManufacturer().getIdentifier();
                identifier.setSystem(vS).setValue(vC);
            }
            
            JsonNode maholder = vaccination.get("maholder");
            if(maholder!=null){
                Extension mah = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVaccineMarketAuthorization");
                String vS = maholder.get("system").asText();
                String vC = maholder.get("code").asText();
                mah.setValue(new Coding(vS, vC,""));
                imm.addExtension(mah);
            }
            
            JsonNode lot = vaccination.get("lot");
            if(lot!=null){
                imm.setLotNumber(lot.asText());
            }
            else{
                addNotFoundIssue("vaccination.lot", out);
            }
            JsonNode date = vaccination.get("date");
            if(date!=null){
                imm.setOccurrence(new DateTimeType(date.asText()));
                immR.setDateElement(new DateTimeType(date.asText()));
            }
            else{
                addNotFoundIssue("vaccination.date", out);
            }
            JsonNode validFrom = vaccination.get("validFrom");
            if(validFrom!=null){
                Extension validF = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVaccineValidFrom");
                String value = validFrom.asText();
                validF.setValue(new DateType(value));
                imm.addExtension(validF);
            }
            
            JsonNode dose = vaccination.get("dose");
            if(dose!=null){
                imm.getProtocolAppliedFirstRep().setDoseNumber(new PositiveIntType(dose.asInt()));
                immR.getRecommendationFirstRep().setDoseNumber(new PositiveIntType(dose.asInt()));
            }
            else{
                addNotFoundIssue("vaccination.dose", out);
            }
            
            JsonNode totalDoses = vaccination.get("totalDoses");
            if(totalDoses!=null){
                imm.getProtocolAppliedFirstRep().setSeriesDoses(new PositiveIntType(totalDoses.asInt()));
                immR.getRecommendationFirstRep().setSeriesDoses(new PositiveIntType(dose.asInt()));
                
            }
            
            JsonNode country = vaccination.get("country");
            if(country!=null){
                Extension cou = new Extension("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCCountryOfEvent");
                //String vS = country.get("system").asText();
                String vC = country.get("code").asText();
                cou.setValue(new CodeType(vC));
                imm.addExtension(cou);
            }
            else{
                addNotFoundIssue("vaccination.country", out);
            }
            
            JsonNode centre = vaccination.get("centre");
            if(centre!=null){
                imm.getLocation().setDisplay(centre.asText());
            }
            else{
                addNotFoundIssue("vaccination.centre", out);
            }
            
            JsonNode disease = vaccination.get("disease");
            if(disease!=null){
                String vS = disease.get("system").asText();
                String vC = disease.get("code").asText();
                imm.getProtocolAppliedFirstRep().getTargetDiseaseFirstRep().addCoding(new Coding(vS, vC, ""));
            }
            JsonNode nextDose = vaccination.get("nextDose");
            if(nextDose!=null){
                Date readValue = df.parse(nextDose.asText());
                immR.getRecommendationFirstRep().getDateCriterionFirstRep().setValueElement(new DateTimeType(nextDose.asText()));
            }
            
            
        }catch(Exception ex){
            System.out.println("ex = " + ex.getMessage());
            addErrorIssue("vaccination", ex.getMessage(), out);
            
        }
        
        if(out.getIssue().size()>0){
            String resourceToString = HapiFhirTools.resourceToString(out);
            log.error(resourceToString);
            return resourceToString;
        }
        
        
        
        comp.setStatus(Composition.CompositionStatus.FINAL);
        comp.setType(new CodeableConcept(new Coding("http://loinc.org","82593-5", null)));
        comp.getCategoryFirstRep().addCoding(
        new Coding("http://worldhealthorganization.github.io/ddcc/CodeSystem/DDCC-Composition-Category-CodeSystem",
        "ddcc-vs",null));
        comp.setDate(new Date());
        comp.getIdentifier().setSystem("http://acme.org/idcomposition");
        comp.getIdentifier().setValue("123617826318673");
        comp.getSectionFirstRep().setCode(new CodeableConcept(new Coding("http://loinc.org","11369-6", null)));
        comp.getSectionFirstRep().setFocus(immRef);
        comp.setSubject(patRef);
        comp.setTitle("Digital Documentation of COVID-19 Certificate (DDCC)");
        comp.getSectionFirstRep().addEntry(immRef);
        comp.getSectionFirstRep().addEntry(docRRef);
        
        
        
        
        //b.addEntry().setFullUrl(patRef.getReference()).setResource(pat);
        addBundleEntryComponentToAddBundle(comp,compRef,b);
        addBundleEntryComponentToAddBundle(pat,patRef,b);
        addBundleEntryComponentToAddBundle(imm,immRef,b);
        addBundleEntryComponentToAddBundle(docR,docRRef,b);
        addBundleEntryComponentToAddBundle(org, orgRef, b);
        if(immR.getRecommendationFirstRep().getDateCriterionFirstRep().getValue()!=null){
            comp.getSectionFirstRep().addEntry(immRRef);
            addBundleEntryComponentToAddBundle(immR,immRRef,b);
        }
        String resourceToString = HapiFhirTools.resourceToString(b);
        return resourceToString;
    }
    
    void addBundleEntryComponentToAddBundle(Resource r, Reference ref, Bundle b){
        Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
        entry.setResource(r);
        entry.setFullUrl(ref.getReference());
        Bundle.BundleEntryRequestComponent request = entry.getRequest();
        request.setMethod(Bundle.HTTPVerb.PUT);
        request.setUrl(r.getResourceType().name()+"/"+r.getId());
        b.addEntry(entry);
        
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
    
    void addErrorIssue(String value, String message, OperationOutcome out){
        OperationOutcome.OperationOutcomeIssueComponent issue;
        issue = new OperationOutcome.OperationOutcomeIssueComponent();
        issue.setCode(OperationOutcome.IssueType.EXCEPTION);
        issue.setDiagnostics(value+" have errors in definition ["+message+"]");
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
    
    
    public String resourcesToVSCoreDataSet(Bundle entry){
        
        OperationOutcome out = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue;
        
        Bundle.BundleEntryComponent find = HapiFhirTools.findEntryByResourceClassAndRemove(entry, Composition.class,"PUT");
        Composition comp = null;
        if(find!=null)
            comp = (Composition) find.getResource();
        else 
            addNotFoundIssue("Composition resource", out);
        find = HapiFhirTools.findEntryByResourceClassAndRemove(entry, Patient.class,"PUT");
        Patient pat = null;
        if(find!=null)
            pat = (Patient) find.getResource();
        else 
            addNotFoundIssue("Patient resource", out);
        find = HapiFhirTools.findEntryByResourceClassAndRemove(entry, Immunization.class,"PUT");
        Immunization imm=null;
        if(find!=null)
            imm = (Immunization) find.getResource();
        else 
            addNotFoundIssue("Immunization resource", out);
        find = HapiFhirTools.findEntryByResourceClassAndRemove(entry, ImmunizationRecommendation.class,"PUT");
        ImmunizationRecommendation immR = null;
        if(find!=null)
            immR = (ImmunizationRecommendation) find.getResource();
        
        find = HapiFhirTools.findEntryByResourceClassAndRemove(entry, Organization.class,"PUT");
        Organization org = null;
        if(find!=null)
            org = (Organization) find.getResource();
        else 
            addNotFoundIssue("Organization resource", out);
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("resourceType", "DDCCCoreDataSet");
        
        ObjectNode certificate = JsonNodeFactory.instance.objectNode();
        node.putPOJO("certificate", certificate);
        ObjectNode certificatePeriod = JsonNodeFactory.instance.objectNode();
        certificate.putPOJO("period", certificatePeriod);
        ObjectNode vaccination = JsonNodeFactory.instance.objectNode();
        node.putPOJO("vaccination", vaccination);
        
        
        
        HumanName name = pat.getNameFirstRep();
        if(name!=null && name.getText()!=null){
            node.put("name",name.getText());
        }
        else
            addNotFoundIssue("bundle.entry[Patient].name[0].text", out);
        
        DateType birth = pat.getBirthDateElement();
        if(birth!=null)
            node.put("birthDate",birth.asStringValue());
        
        Identifier ident = pat.getIdentifierFirstRep();
        if(ident!=null)
            node.put("identifier",ident.getValue());
        
        Immunization.ImmunizationProtocolAppliedComponent protocolApplied = imm.getProtocolAppliedFirstRep();
        if(protocolApplied!=null){
            Reference authority = protocolApplied.getAuthority();
            PositiveIntType doseNumber = protocolApplied.getDoseNumberPositiveIntType();
            PositiveIntType seriesDoses = protocolApplied.getSeriesDosesPositiveIntType();
            Coding disCoding = protocolApplied.getTargetDiseaseFirstRep().getCodingFirstRep();
            if(authority.getIdentifier().getValue()!=null)
                certificate.put("issuer", authority.getIdentifier().getValue());
            else if(org!=null && org.getName()!=null)
                certificate.put("issuer", org.getName());
            else
               addNotFoundIssue("bundle.entry[Immunization].protocolApplied[0].authority.reference.identifier and Organization.name", out);
            if(doseNumber.asStringValue()!=null)
               vaccination.put("dose",doseNumber.asStringValue());
            else
               addNotFoundIssue("bundle.entry[Immunization].protocolApplied[0].doseNumber", out);
            if(seriesDoses.asStringValue()!=null)
               vaccination.put("totalDoses",seriesDoses.asStringValue());
            if(disCoding!=null){
                vaccination.putRawValue("disease",new RawValue("{\"system\":\""+
                        disCoding.getSystem()+"\",\"code\":\""+
                        disCoding.getCode()+"\"}"));
            }
        }
        else{
            addNotFoundIssue("bundle.entry[Immunization].protocolApplied[0]", out);
        }
        
        Bundle.BundleLinkComponent link = entry.getLinkFirstRep();
        if(link!=null && link.getUrl()!=null)
            certificate.put("hcid", link.getUrl().replace("urn:HCID:", ""));
        else
            addNotFoundIssue("bundle.link[0].url", out); 
        certificate.put("ddccid", entry.getId());
        certificate.put("version","RC2");
        
        Composition.CompositionEventComponent event = comp.getEventFirstRep();
        if(event!=null){
            Period period = event.getPeriod();
            if(period!=null){
                if(period.getStartElement().asStringValue()!=null)
                    certificatePeriod.put("start",period.getStartElement().asStringValue());
                if(period.getEndElement().asStringValue()!=null)
                    certificatePeriod.put("end",period.getEndElement().asStringValue());
            }
        }
        
        CodeableConcept vaccineCode = imm.getVaccineCode();
        if(vaccineCode!=null){
            Coding coding = vaccineCode.getCodingFirstRep();
            if(coding!=null){
                vaccination.putRawValue("vaccine",new RawValue("{\"system\":\""+
                        coding.getSystem()+"\",\"code\":\""+
                        coding.getCode()+"\"}"));
            }
            else
                addNotFoundIssue("bundle.entry[Immunization].vaccineCode.coding[0]", out);
        }
        else
            addNotFoundIssue("bundle.entry[Immunization].vaccineCode", out);
        
        Extension brandEx = imm.getExtensionByUrl("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCEventBrand");
        if(brandEx!=null){
            Coding coding = (Coding) brandEx.getValue();
            if(coding!=null){
                vaccination.putRawValue("brand",new RawValue("{\"system\":\""+
                        coding.getSystem()+"\",\"code\":\""+
                        coding.getCode()+"\"}"));
            }
            else
                addNotFoundIssue("bundle.entry[Immunization].extension[DDCCVaccineBrand].valueCoding",out);
        }
        else 
            addNotFoundIssue("bundle.entry[Immunization].extension[DDCCVaccineBrand]", out);
        Reference manufacturer = imm.getManufacturer();
        if(manufacturer!=null){
            Identifier iden = manufacturer.getIdentifier();
            vaccination.putRawValue("manufacturer",new RawValue("{\"system\":\""+
                        iden.getSystem()+"\",\"code\":\""+
                        iden.getValue()+"\"}"));
        }
            
        Extension maHolderEx = imm.getExtensionByUrl("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVaccineMarketAuthorization");
        if(maHolderEx!=null){
            Coding coding = (Coding) maHolderEx.getValue();
            if(coding!=null){
                vaccination.putRawValue("maholder",new RawValue("{\"system\":\""+
                        coding.getSystem()+"\",\"code\":\""+
                        coding.getCode()+"\"}"));
            }
        }   
        
        String lotNumber = imm.getLotNumber();
        if(lotNumber!=null)
            vaccination.put("lot",lotNumber);
        else
            addNotFoundIssue("bundle.entry[Immunization].lotNumber", out);
        
        DateTimeType occurrence = imm.getOccurrenceDateTimeType();
        if(occurrence!=null)
            vaccination.put("date",occurrence.asStringValue());
        else
            addNotFoundIssue("bundle.entry[Immunization].occurrence", out);
           
        Extension validFromEx = imm.getExtensionByUrl("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCVaccineValidFrom");
        if(maHolderEx!=null){
            DateType date = (DateType) validFromEx.getValue();
            if(date!=null){
                vaccination.put("validFrom",date.asStringValue());
            }
        } 
        
        Extension countryEx = imm.getExtensionByUrl("http://worldhealthorganization.github.io/ddcc/StructureDefinition/DDCCCountryOfVaccination");
        if(countryEx!=null){
            Coding coding = (Coding) countryEx.getValue();
            if(coding!=null){
                vaccination.putRawValue("country",new RawValue("{\"system\":\""+
                        coding.getSystem()+"\",\"code\":\""+
                        coding.getCode()+"\"}"));
            }
            else
                addNotFoundIssue("bundle.entry[Immunization].extension[DDCCCountryOfVaccination].valueCoding",out);
        }
        else 
            addNotFoundIssue("bundle.entry[Immunization].extension[DDCCCountryOfVaccination].valueCoding", out);
        
        String display = imm.getLocation().getDisplay();
        if(display!=null)
            vaccination.put("centre", display);
        else
            addNotFoundIssue("bundle.entry[Immunization].location.display",out);
        
        Reference actor = imm.getPerformerFirstRep().getActor();
        if(actor!=null)
            vaccination.putRawValue("practitioner",new RawValue("{\"value\":\""+actor.getIdentifier().getValue()+"\"}"));
        
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent dateCriterion =
                immR.getRecommendationFirstRep().getDateCriterionFirstRep();
        if(dateCriterion!=null)
            vaccination.put("nextDose", dateCriterion.getValueElement().asStringValue());
        Enumerations.AdministrativeGender gender = pat.getGender();
        if(gender != null)
            node.put("sex", gender.toCode());
        
        
        if(out.getIssue().size()>0){
            String resourceToString = HapiFhirTools.resourceToString(out);
            log.error(resourceToString);
            return resourceToString;
        }
        return node.toString();
    }
}
