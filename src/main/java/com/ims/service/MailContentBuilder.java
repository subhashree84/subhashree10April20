package com.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {
 
    private TemplateEngine templateEngine;
 
    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
 
    public String build(String panelDetails,String name,String feedbackURL,String mailTemplate) {
        Context context = new Context();
        context.setVariable("panelDetails", panelDetails);
        context.setVariable("feedbackURL", feedbackURL);
        context.setVariable("name", name);
        return templateEngine.process(mailTemplate, context);
    }
    
    public String smsBuild(String panelDetails,String name,String mailTemplate) {
        Context context = new Context();
        context.setVariable("panelDetails", panelDetails);
        context.setVariable("name", name);
        return templateEngine.process(mailTemplate, context);
    }
    
    public String smsBuildForSelectedOrRejected(String eventName,String panelDetails,String name,String candidateStatus,String mailTemplate) {
        Context context = new Context();
        context.setVariable("eventName", eventName);
        context.setVariable("panelDetails", panelDetails);
        context.setVariable("name", name);
        context.setVariable("candidateStatus", candidateStatus);
        return templateEngine.process(mailTemplate, context);
    }
    
    public String candidateMailTemplate(String panelDetails,String name,String mailTemplate) {
        Context context = new Context();
        context.setVariable("panelDetails", panelDetails);
        context.setVariable("name", name);
        return templateEngine.process(mailTemplate, context);
    }
 
}
