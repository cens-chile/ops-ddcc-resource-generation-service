/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.MethodNotAllowedException;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Resource;

/**
 *
 * @author José <jose.m.andrade@gmail.com>
 */
public final class HapiFhirTools {
    
    
    public static void printResource(Resource r, Class clazz){
        
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);
        String serialized = parser.encodeResourceToString(r);
        System.out.println(serialized);
    }
    public static String resourceToString(Resource r){
        
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);
        String serialized = parser.encodeResourceToString(r);
        return serialized;
    }
    
    public static Bundle.BundleEntryComponent createEntryResponseComponent(
            String status, String etag,String location, InstantType date){
        
            Bundle.BundleEntryComponent entry = new Bundle.BundleEntryComponent();
            Bundle.BundleEntryResponseComponent response = new Bundle.BundleEntryResponseComponent();
            response.setStatus(status);
            response.setEtag(etag);
            response.setLocation(location);
            response.setLastModifiedElement(date);
            entry.setResponse(response);
            return entry;
    }
    
    public static boolean validateMethod(Bundle.BundleEntryComponent b, String method){
        
        if(!b.getRequest().isEmpty() && b.getRequest().getMethod()!=null
                && b.getRequest().getMethod().name().equals(method))
            return true;
        return false;
    }
    
    public static String getProfile(Meta meta)
    {
        String profile = null;
        if(meta!=null){
            List<CanonicalType> profiles = meta.getProfile();
            if(profiles!=null && profiles.size()>0)
                profile = profiles.get(0).asStringValue();
        }
        return profile;
    }
    
    public static Bundle.BundleEntryComponent findEntryByResourceClassAndRemove(Bundle bundle,Class resourceType,String method)
    {
       
        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
        for (Bundle.BundleEntryComponent entry : entries){
            if(entry.getResource().getResourceType().name().equals(resourceType.getSimpleName())){
                boolean validateMethod = HapiFhirTools.validateMethod(entry, method);
                if(!validateMethod)
                    throw new MethodNotAllowedException("There is an unsupported operation in Bundle.");
                entries.remove(entry);
                return entry;
            }
        }
        return null;
    }
    
    public static void addProfileToResource(Resource r, String profile)
    {
        r.getMeta().getProfile().add(new CanonicalType(profile));
    }
    
}
