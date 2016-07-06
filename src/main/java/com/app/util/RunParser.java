package com.app.util;

import com.app.parser.McaGovIn;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;

public class RunParser implements Job {

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        System.out.println("Parser is running!");
        List<String> allCIN = McaGovIn.getAllCIN("https://www.zaubacorp.com/company-list/p-1-company.html");
        McaGovIn.getCompanies(allCIN);
    }
}
