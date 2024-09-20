package com.cens.generationService;

import com.cens.generationService.fhirServer.FhirRestfulServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
//@ServletComponentScan (basePackages = "com")
@ServletComponentScan(basePackageClasses = {
  FhirRestfulServer.class})
public class GenerationServiceApplication extends SpringBootServletInitializer{

    @Autowired
    ApplicationProperties properties;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    FhirRestfulServer fhirRestfulServer;
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GenerationServiceApplication.class);
    }
    public static void main(String[] args) {
            SpringApplication.run(GenerationServiceApplication.class, args);
    }

    @Autowired
    ApplicationProperties appProperties;
    @Autowired
    AutowireCapableBeanFactory beanFactory;
    
    @Bean
    public ServletRegistrationBean hapiServletRegistration() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        //DistribuidorFhirRestfulServer distribuidorFhirRestfulServer = new DistribuidorFhirRestfulServer(properties);
        fhirRestfulServer = new FhirRestfulServer();
        beanFactory.autowireBean(fhirRestfulServer);
        servletRegistrationBean.setServlet(fhirRestfulServer);
        servletRegistrationBean.addUrlMappings(String.format("/%s/*", appProperties.getBaseUrl()));
        servletRegistrationBean.setLoadOnStartup(1);

        return servletRegistrationBean;
    }
    
    @Bean
  public ServletRegistrationBean overlayRegistrationBean() {

    AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext = new AnnotationConfigWebApplicationContext();

    DispatcherServlet dispatcherServlet = new DispatcherServlet(
      annotationConfigWebApplicationContext);
    dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);

    ServletRegistrationBean registrationBean = new ServletRegistrationBean();
    registrationBean.setServlet(dispatcherServlet);
    registrationBean.addUrlMappings("/*");
    registrationBean.setLoadOnStartup(1);
    return registrationBean;

  }
}
