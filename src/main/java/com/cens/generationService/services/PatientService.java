/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cens.generationService.services;

import ca.uhn.fhir.rest.api.MethodOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
@Service
public class PatientService {
    
    private static final  Logger log = LoggerFactory.getLogger(PatientService.class); 
    @Autowired
    RequestSenderToFhirServer request;
    
    public Patient createPatient(Patient patient)
    {
        try{
            MethodOutcome outcome =null;//= request.saveResourceToNational(patient);
            if(outcome.getResource()!=null)
                return (Patient) outcome.getResource();
        }
        catch(Exception ex){
            log.error("Error creando Paciente");
        }
        return null;
    }
    
    
    
}
