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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import io.swagger.v3.core.util.Json;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.DocumentReferenceStatus;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import util.HapiFhirTools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Service
public class DVCCoreDataSetService {
    
    private static final  Logger log = LoggerFactory.getLogger(DVCCoreDataSetService.class);


    
    /**
     * 
     * @param qr: QuestionnaireResponse con conjunto minimo de datos.
     * @return ObjectNode 
     */
    public String QRtoDVCCoreDataSetStringJson(QuestionnaireResponse qr){
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> items = qr.getItem();
        OperationOutcome out = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue;

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("resourceType", "ModelDVC");

        //name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = findItem("name",items);
        if(item == null || !item.getAnswerFirstRep().hasValueStringType() || !item.getAnswerFirstRep().getValueStringType().hasValue()){
            addNotFoundIssue("name", out);
        }
        else{
            String name = getStringFromItem("name", item, out);
            if(name != null){
                node.put("name", name);
            }
        }
        //dob
        item = findItem("dob",items);
        if(item == null || !item.getAnswerFirstRep().hasValueDateType() || !item.getAnswerFirstRep().getValueDateType().hasValue()){
            addNotFoundIssue("dob", out);
        }
        else{
            String dob = getStringFromItemDate("dob", item, out);
            if(dob!=null){
                node.put("dob",dob);
            }
        }
        //sex
        item = findItem("sex",items);
        if(item != null ){
            ObjectNode json = getValueCodingFromItemInJsonFormat("sex", item, out);
            if(json!=null){
                String code  = json.get("code").asText();
                node.put("sex",code);
            }
        }
        //nationality
        item = findItem("nationality",items);
        if(item != null ){
            ObjectNode json = getValueCodingFromItemInJsonFormat("nationality", item, out);
            if(json!=null){
                String code  = json.get("code").asText();
                node.put("nationality",code);
            }
        }
        //nid
        item = findItem("nid",items);
        if(item != null){
            String nid = getStringFromItem("nid", item, out);
            if(nid != null){
                node.put("nid",nid);
            }
        }
        //guardian
        item = findItem("guardian",items);
        if(item != null){
            List<QuestionnaireResponse.QuestionnaireResponseItemComponent> guardianItems = item.getItem();
            QuestionnaireResponse.QuestionnaireResponseItemComponent guardianNameitem = findItem("guardianName",guardianItems);
            QuestionnaireResponse.QuestionnaireResponseItemComponent guardianRelationshipitem = findItem("guardianRelationship",guardianItems);
            String finalGuardian = "";
            if(guardianRelationshipitem != null) {
                ObjectNode guardianRelationship =  getValueCodingFromItemInJsonFormat("guardianRelationship", guardianRelationshipitem, out);
                if(guardianRelationship != null){
                    finalGuardian = String.format("%s/", guardianRelationship.get("code").asText());
                }
            }
            if(guardianNameitem != null) {
                String guardianName = getStringFromItem("guardianName", guardianNameitem, out);
                if(guardianName != null){
                    finalGuardian = String.format("%s%s", finalGuardian, guardianName);
                    node.put("guardian",finalGuardian);
                }
            }

        }
        //vaccineDetails
        item = findItem("vaccineDetails",items);
        if(item == null || !item.hasItem()){
            addNotFoundIssue("vaccineDetails", out);
        }
        else{
            List<QuestionnaireResponse.QuestionnaireResponseItemComponent> vaccineDetailsItems = item.getItem();
            ObjectNode vaccineDetails = JsonNodeFactory.instance.objectNode();
            ArrayNode vaccineDetailsArray = node.putArray("vaccineDetails");
            vaccineDetailsArray.add(vaccineDetails);
            vaccineDetails.put("resourceType", "ModelVaccineDetails");

            //doseNumber
            item = findItem("doseNumber",vaccineDetailsItems);
            if(item == null || !item.getAnswerFirstRep().hasValueCoding() || !item.getAnswerFirstRep().getValueCoding().hasCode()){
                addNotFoundIssue("doseNumber", out);
            }
            else{
                ObjectNode json = getValueCodingFromItemInJsonFormat("doseNumber", item, out);
                if(json!=null){
                    ObjectNode codeableConcept = JsonNodeFactory.instance.objectNode();
                    ArrayNode coding = codeableConcept.putArray("coding");
                    coding.add(json);
                    vaccineDetails.putPOJO("doseNumber",codeableConcept);
                }
            }

            //disease
            item = findItem("disease",vaccineDetailsItems);
            if(item == null || !item.getAnswerFirstRep().hasValueCoding() || !item.getAnswerFirstRep().getValueCoding().hasCode()){
                addNotFoundIssue("disease", out);
            }
            else{
                ObjectNode json = getValueCodingFromItemInJsonFormat("disease", item, out);
                if(json!=null){
                    vaccineDetails.putPOJO("disease",json);
                }
            }

            //vaccineClassification
            item = findItem("vaccineClassification",vaccineDetailsItems);
            if(item == null || !item.getAnswerFirstRep().hasValueCoding() || !item.getAnswerFirstRep().getValueCoding().hasCode()){
                addNotFoundIssue("vaccineClassification", out);
            }
            else{
                ObjectNode json = getValueCodingFromItemInJsonFormat("vaccineClassification", item, out);
                if(json!=null){
                    ObjectNode codeableConcept = JsonNodeFactory.instance.objectNode();
                    ArrayNode coding = codeableConcept.putArray("coding");
                    coding.add(json);
                    vaccineDetails.putPOJO("vaccineClassification",codeableConcept);
                }
            }
            //vaccineTradeItem
            item = findItem("vaccineTradeItem",vaccineDetailsItems);
            if(item != null){
                String vaccineTradeItem = getStringFromItem("vaccineTradeItem", item, out);
                if(vaccineTradeItem != null){
                    ObjectNode vaccineTradeItemNode = JsonNodeFactory.instance.objectNode();
                    vaccineTradeItemNode.put("system", "id");
                    vaccineTradeItemNode.put("value", vaccineTradeItem);
                    vaccineDetails.putPOJO("vaccineTradeItem", vaccineTradeItemNode);
                }
            }
            //date
            item = findItem("date",vaccineDetailsItems);
            if(item == null || !item.getAnswerFirstRep().hasValueDateType() || !item.getAnswerFirstRep().getValueDateType().hasValue()){
                addNotFoundIssue("date", out);
            }
            else{
                String date = getStringFromItemDate("date", item, out);
                if(date!=null){
                    vaccineDetails.put("date",date);
                }
            }
            //clinicianName
            item = findItem("clinicianName",vaccineDetailsItems);
            if (item != null){
                String clinicianName = getStringFromItem("clinicianName", item, out);
                if(clinicianName != null){
                    vaccineDetails.put("clinicianName",clinicianName);
                }
            }
            //issuer
            item = findItem("issuer",vaccineDetailsItems);
            if (item != null){
                String issuerItem = getStringFromItem("issuer", item, out);
                if(issuerItem != null){
                    ObjectNode issuerItemNode = JsonNodeFactory.instance.objectNode();
                    issuerItemNode.put("reference", "ref");
                    issuerItemNode.put("display", issuerItem);
                    vaccineDetails.putPOJO("issuer", issuerItemNode);
                }
            }
            //manufacturerId
            item = findItem("manufacturerId",vaccineDetailsItems);
            if (item != null){
                String manufacturerIdItem = getStringFromItem("manufacturerId", item, out);
                ObjectNode manufacturerIdItemNode = JsonNodeFactory.instance.objectNode();
                if(manufacturerIdItem != null ){
                    manufacturerIdItemNode.put("system", "id-manufacturer");
                    manufacturerIdItemNode.put("value", manufacturerIdItem);
                    vaccineDetails.putPOJO("manufacturerId", manufacturerIdItemNode);
                }
            }
            //manufacturer
            item = findItem("manufacturer",vaccineDetailsItems);
            if(item == null || !item.getAnswerFirstRep().hasValueStringType() || !item.getAnswerFirstRep().getValueStringType().hasValue()){
                addNotFoundIssue("manufacturer", out);
            }
            else{
                String manufacturerItem = getStringFromItem("manufacturer", item, out);
                if(manufacturerItem != null ){
                    vaccineDetails.put("manufacturer", manufacturerItem);
                }
            }
            //batchNo
            item = findItem("batchNo",vaccineDetailsItems);
            if(item == null || !item.getAnswerFirstRep().hasValueStringType() || !item.getAnswerFirstRep().getValueStringType().hasValue()){
                addNotFoundIssue("batchNo", out);
            }
            else{
                String batchNoItem = getStringFromItem("batchNo", item, out);
                if(batchNoItem != null ){
                    vaccineDetails.put("batchNo", batchNoItem);
                }
            }
            //validityPeriod
            ObjectNode validityPeriod = JsonNodeFactory.instance.objectNode();
            item = findItem("validityPeriod",vaccineDetailsItems);
            List<QuestionnaireResponse.QuestionnaireResponseItemComponent> validityPeriodItems = item.getItem();
            QuestionnaireResponse.QuestionnaireResponseItemComponent startitem = findItem("startDate",validityPeriodItems);
            QuestionnaireResponse.QuestionnaireResponseItemComponent enditem = findItem("endDate",validityPeriodItems);
            if(startitem != null ){
                String start = getStringFromItemDate("start", startitem, out);
                if(start!=null){
                    validityPeriod.put("start",start);
                }
            }
            if(enditem != null ){
                String end = getStringFromItemDate("end", enditem, out);
                if(end!=null){
                    validityPeriod.put("end",end);
                }
            }
            vaccineDetails.putPOJO("validityPeriod", validityPeriod);

        }

        if(!out.getIssue().isEmpty()){
            String resourceToString = HapiFhirTools.resourceToString(out);
            log.error(resourceToString);
            return resourceToString;
        }
        return node.toString();
    }
    
    
    
    public String transformDVCCoreDataSetToAddBundle(String core) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        OperationOutcome out = new OperationOutcome();

        JsonNode node;
        try {
            node = mapper.readTree(core);
            String toString = node.toString();

        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(DVCCoreDataSetService.class.getName()).log(Level.SEVERE, null, ex);
            throw new FHIRException(ex.getMessage());
        }
        JsonNode vaccineDetailsList = node.get("vaccineDetails");
        JsonNode vaccineDetails = JsonNodeFactory.instance.objectNode();
        if (vaccineDetailsList.isArray()){
            vaccineDetails = vaccineDetailsList.get(0);
        }
        Bundle b = new Bundle();
        Composition com = new Composition();
        IdType comId = IdType.newRandomUuid();
        b.addEntry()
                .setFullUrl(comId.getIdPart())
                .setResource(com);
        Patient pat = new Patient();
        IdType patId = IdType.newRandomUuid();
        b.addEntry()
                .setFullUrl(patId.getIdPart())
                .setResource(pat);
        Immunization imm = new Immunization();
        IdType immId = IdType.newRandomUuid();
        b.addEntry()
                .setFullUrl(immId.getIdPart())
                .setResource(imm);
        Practitioner pra = new Practitioner();
        IdType praId = IdType.newRandomUuid();
        Organization org = new Organization();
        IdType orgId = IdType.newRandomUuid();
        Organization orgManufacturer = new Organization();
        IdType orgManufacturerId = IdType.newRandomUuid();
        b.addEntry()
                .setFullUrl(orgManufacturerId.getIdPart())
                .setResource(orgManufacturer);
        HapiFhirTools.addProfileToResource(b, "http://smart.who.int/icvp/StructureDefinition/DVCBundle");
        HapiFhirTools.addProfileToResource(com, "http://smart.who.int/icvp/StructureDefinition/DVCComposition");
        HapiFhirTools.addProfileToResource(pat, "http://smart.who.int/icvp/StructureDefinition/DVCPatient");
        HapiFhirTools.addProfileToResource(imm, "http://smart.who.int/icvp/StructureDefinition/DVC-ImmunizationUvIps");
        HapiFhirTools.addProfileToResource(pra, "https://profiles.ihe.net/ITI/mCSD/StructureDefinition/IHE.mCSD.Practitioner");
        HapiFhirTools.addProfileToResource(org, "https://profiles.ihe.net/ITI/mCSD/StructureDefinition/IHE.mCSD.JurisdictionOrganization");
        // Bundle
        b.setType(Bundle.BundleType.DOCUMENT);
        IdType bunId = IdType.newRandomUuid();
        b.getIdentifier().setValue(bunId.getValue());
        b.setTimestamp(new Date());

        // Patient
        pat.setId(patId.getIdPart().split(":")[2]);
        pat.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
        pat.getText().setDivAsString(String.format("<div>This is the Patient<br/>id: %s</div>", patId.getValue()));
        JsonNode get = node.get("name");
        if (get != null) {
            pat.getNameFirstRep().setUse(HumanName.NameUse.OFFICIAL);
            pat.getNameFirstRep().setText(get.asText());
        } else {
            addNotFoundIssue("name", out);
        }
        get = node.get("nid");
        if (get != null) {
            pat.getIdentifierFirstRep()
                    .setSystem("NID")
                    .setValue(get.asText());
        } else {
            addNotFoundIssue("nid", out);
        }
        get = node.get("dob");
        if (get != null)
            pat.setBirthDateElement(new DateType(get.asText()));
        get = node.get("sex");
        if (get != null) {
            pat.setGender(getFhirGender(get.asText()));
        }
        get = node.get("nationality");
        Extension exNationality = pat.addExtension();
        if (get != null) {
            exNationality.setUrl("http://hl7.org/fhir/StructureDefinition/patient-nationality");
            Extension exCode = exNationality.addExtension();
            CodeableConcept cc = new CodeableConcept();
            cc.getCodingFirstRep()
                    .setSystem("urn:iso:std:iso:3166")
                    .setCode(get.asText());
            exCode.setUrl("code");
            exCode.setValue(cc);
        }
        get = node.get("guardian");
        if (get != null) {
            String[] split = get.asText().split("/");
            if (split.length > 1) {
                pat.getContactFirstRep().getRelationshipFirstRep().getCodingFirstRep().setCode(split[0]);
            } else {
                pat.getContactFirstRep().getName().setText(split[1]);
            }
        }

        // Immunization
        imm.setId(immId.getIdPart().split(":")[2]);
        imm.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
        imm.getText().setDivAsString(String.format("<div>This is the Immunization<br/>id: %s</div>", immId.getValue()));
        imm.setStatus(ImmunizationStatus.COMPLETED);
        imm.getPatient().setReferenceElement(patId);
        get = vaccineDetails.get("date");
        if (get != null){
            try {
                Date readValue = df.parse(get.asText());
                imm.getOccurrenceDateTimeType().setValue(readValue, TemporalPrecisionEnum.DAY);
            } catch (Exception ex) {
                addErrorIssue("date", ex.getMessage(), out);
            }
        }
        get = vaccineDetails.get("batchNo");
        if (get != null) {
            imm.setLotNumber(get.asText());
        }
        imm.getPerformerFirstRep().getActor().setReferenceElement(praId);
        JsonNode disease = vaccineDetails.get("disease");
        if (disease != null) {
            JsonNode diseaseSystem = disease.get("system");
            JsonNode diseaseCode = disease.get("code");
            JsonNode diseaseDisplay = disease.get("display");
            Coding diseaseCoding = new Coding();
            if (diseaseSystem != null) {
                diseaseCoding.setSystem(diseaseSystem.asText());
            }
            if (diseaseCode != null) {
                diseaseCoding.setCode(diseaseCode.asText());
            }
            if (diseaseDisplay != null) {
                diseaseCoding.setDisplay(diseaseDisplay.asText());
            }
            imm.getProtocolAppliedFirstRep().getTargetDiseaseFirstRep().addCoding(diseaseCoding);
        }
        JsonNode doseNumberNode = vaccineDetails.get("doseNumber");
        if (doseNumberNode != null) {
            JsonNode doseNumberCodingList = doseNumberNode.get("coding");
            if (doseNumberCodingList.isArray()){
                for(JsonNode doseNumber: doseNumberCodingList){
                    Coding doseNumberCoding = new Coding();
                    if (doseNumber != null) {
                        JsonNode doseNumberSystem = doseNumber.get("system");
                        JsonNode doseNumberCode = doseNumber.get("code");
                        JsonNode doseNumberDisplay = doseNumber.get("display");
                        if (doseNumberSystem != null) {
                            doseNumberCoding.setSystem(doseNumberSystem.asText());
                        }
                        if (doseNumberCode != null) {
                            doseNumberCoding.setCode(doseNumberCode.asText());
                        }
                        if (doseNumberDisplay != null) {
                            doseNumberCoding.setDisplay(doseNumberDisplay.asText());
                        }
                        StringType st = new StringType();
                        Extension doseNumberExtension = st.addExtension();
                        imm.getProtocolAppliedFirstRep().setDoseNumber(st);
                        doseNumberExtension.setUrl("http://smart.who.int/icvp/StructureDefinition/doseNumberCodeableConcept");
                        CodeableConcept doseNumberCodeableConcept = new CodeableConcept();
                        doseNumberCodeableConcept.addCoding(doseNumberCoding);
                        doseNumberExtension.setValue(doseNumberCodeableConcept);
                    }
                }
            }
        }
        JsonNode vaccineClassificationNode = vaccineDetails.get("vaccineClassification");
        if (vaccineClassificationNode != null) {
            JsonNode vaccineClassificationCodingList = vaccineClassificationNode.get("coding");
            if (vaccineClassificationCodingList.isArray()){
                for(JsonNode vaccineClassification: vaccineClassificationCodingList){
                    Coding vaccineClassificationCoding = new Coding();
                    if (vaccineClassification != null) {
                        JsonNode doseNumberSystem = vaccineClassification.get("system");
                        JsonNode doseNumberCode = vaccineClassification.get("code");
                        JsonNode doseNumberDisplay = vaccineClassification.get("display");
                        if (doseNumberSystem != null) {
                            vaccineClassificationCoding.setSystem(doseNumberSystem.asText());
                        }
                        if (doseNumberCode != null) {
                            vaccineClassificationCoding.setCode(doseNumberCode.asText());
                        }
                        if (doseNumberDisplay != null) {
                            vaccineClassificationCoding.setDisplay(doseNumberDisplay.asText());
                        }
                        imm.getVaccineCode().addCoding(vaccineClassificationCoding);
                    }
                }
            }
        }
        JsonNode vaccineTradeItem = vaccineDetails.get("vaccineTradeItem");
        if (vaccineTradeItem != null) {
            JsonNode vaccineTradeItemSystem = vaccineTradeItem.get("system");
            JsonNode vaccineTradeItemValue = vaccineTradeItem.get("value");
            Identifier vaccineTradeItemIdentifier = new Identifier();
            if (vaccineTradeItemSystem != null) {
                vaccineTradeItemIdentifier.setSystem(vaccineTradeItemSystem.asText());
            }
            if (vaccineTradeItemValue != null) {
                vaccineTradeItemIdentifier.setValue(vaccineTradeItemValue.asText());
            }
            Extension vaccineTradeItemExtension = imm.addExtension();
            vaccineTradeItemExtension.setValue(vaccineTradeItemIdentifier);
            vaccineTradeItemExtension.setUrl("http://smart.who.int/icvp/StructureDefinition/vaccineTradeItemIdentifier");
        }

        get = vaccineDetails.get("clinicianName");
        Boolean issuerOrClinician = false;
        if (get != null && !get.asText().isBlank()) {
            // Practitioner
            pra.setId(praId.getIdPart().split(":")[2]);
            //https://profiles.ihe.net/ITI/mCSD/StructureDefinition/IHE.mCSD.Practitioner
            pra.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            pra.getText().setDivAsString(String.format("<div>This is the Practitioner<br/>id: %s</div>", praId.getValue()));
            pra.getNameFirstRep().setText(get.asText());
            b.addEntry()
                    .setFullUrl(praId.getIdPart())
                    .setResource(pra);
            issuerOrClinician = true;
        } else {
            JsonNode issuer = vaccineDetails.get("issuer");
            if(issuer != null){
                JsonNode issuerReference = issuer.get("reference");
                JsonNode issuerDisplay = issuer.get("display");
                if (issuerReference != null && !issuerReference.asText().isBlank()) {
                    org.getIdentifierFirstRep().setValue(issuerReference.asText());
                    org.getIdentifierFirstRep().setSystem("id-org");
                    issuerOrClinician = true;
                }
                if (issuerDisplay != null && !issuerDisplay.asText().isBlank()) {
                    org.setName(issuerDisplay.asText());
                    issuerOrClinician = true;
                }
                // Organization
                org.setActive(true);
                org.setId(orgId.getIdPart().split(":")[2]);
                org.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
                org.getText().setDivAsString(String.format("<div>This is the Organization<br/>id: %s</div>", orgId.getValue()));
                org.getTypeFirstRep().getCodingFirstRep()
                        .setSystem("https://profiles.ihe.net/ITI/mCSD/CodeSystem/IHE.mCSD.Organization.Location.Types")
                        .setCode("jurisdiction");
                b.addEntry()
                        .setFullUrl(orgId.getIdPart())
                        .setResource(org);
            }
        }
        if(!issuerOrClinician){
            addNotFoundIssue("issuerOrClinician", out);
        }

        get = vaccineDetails.get("manufacturer");
        if (get != null && !get.asText().isBlank()) {
            // Organization
            orgManufacturer.setActive(true);
            orgManufacturer.setId(orgManufacturerId.getIdPart().split(":")[2]);
            orgManufacturer.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
            orgManufacturer.getText().setDivAsString(String.format("<div>This is the Organization<br/>id: %s</div>", orgManufacturerId.getValue()));
            orgManufacturer.setName(get.asText());
        }
        else{
            addNotFoundIssue("manufacturer", out);
        }
        JsonNode manufacturerId = vaccineDetails.get("manufacturerId");
        if(get != null) {
            JsonNode manufacturerIdSystem = manufacturerId.get("system");
            JsonNode manufacturerIdValue = manufacturerId.get("value");
            if (manufacturerIdSystem != null && !manufacturerIdSystem.asText().isBlank()) {
                orgManufacturer.getIdentifierFirstRep().setSystem(manufacturerIdSystem.asText());
            }
            if (manufacturerIdValue != null && !manufacturerIdValue.asText().isBlank()) {
                orgManufacturer.getIdentifierFirstRep().setValue(manufacturerIdValue.asText());
            }
        }

        // Composition
        com.setId(comId.getIdPart().split(":")[2]);
        com.getText().setStatus(Narrative.NarrativeStatus.GENERATED);
        com.getText().setDivAsString(String.format("<div>This is the Composition<br/>id: %s</div>", comId.getValue()));
        com.setTitle("DVC Composition");
        com.getIdentifier().setValue(comId.getValue());
        com.setStatus(Composition.CompositionStatus.FINAL);
        com.getType()
                .getCodingFirstRep()
                .setCode("82593-5")
                .setSystem("http://loinc.org");
        com.getSubject().setReferenceElement(patId);
        com.setDate(new Date());
        JsonNode period = vaccineDetails.get("validityPeriod");
        try{
            if (period != null) {
                JsonNode start = period.get("start");
                JsonNode end = period.get("end");
                if (start != null) {
                    Date readValue = df.parse(start.asText());
                    com.getEventFirstRep().getPeriod().setStart(readValue, TemporalPrecisionEnum.DAY);
                }
                if (end != null) {
                    Date readValue = df.parse(end.asText());
                    com.getEventFirstRep().getPeriod().setStart(readValue, TemporalPrecisionEnum.DAY);
                }
            }
        }
        catch (Exception ex) {
            addErrorIssue("period", ex.getMessage(), out);
        }
        com.getSectionFirstRep().getCode().getCodingFirstRep()
                .setCode("11369-6")
                .setSystem("http://loinc.org");
        com.getSectionFirstRep().getFocus().setReferenceElement(immId);
        com.getSectionFirstRep().getEntryFirstRep().setReferenceElement(immId);
        com.getAuthorFirstRep().setReferenceElement(orgId);

        String resourceToString;
        if (!out.getIssue().isEmpty()) {
            resourceToString = HapiFhirTools.resourceToString(out);
            log.error(resourceToString);
            return resourceToString;
        }
        resourceToString = HapiFhirTools.resourceToString(b);
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
    Enumerations.AdministrativeGender getFhirGender(String sex){
        switch (sex){
            case "M": return Enumerations.AdministrativeGender.MALE;
            case "F": return Enumerations.AdministrativeGender.FEMALE;
            case "O": return Enumerations.AdministrativeGender.OTHER;
            case "U": return Enumerations.AdministrativeGender.UNKNOWN;
            case "A": return Enumerations.AdministrativeGender.OTHER;
            case "N": return Enumerations.AdministrativeGender.UNKNOWN;
            case "X": return Enumerations.AdministrativeGender.OTHER;
        }
        return Enumerations.AdministrativeGender.NULL;
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
