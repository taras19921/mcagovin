package com.app.util;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class QuartzListener implements ServletContextListener {

    private ApplicationContext applicationContext;
    Scheduler scheduler = null;

    private ApplicationContext getContext(ServletContextEvent event) {
        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(event.getServletContext());
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContext) {

        System.out.println("Context Initialized");

        try {
            // Setup the Job class and the Job group
            JobDetail job = newJob(RunParser.class).withIdentity(
                    "CronQuartzJob", "Group").build();

            // Create a Trigger that fires every day.
            Trigger trigger = newTrigger()
                    .withIdentity("TriggerName", "Group")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 38 14 * * ?"))
                    .build();

            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContext) {
        System.out.println("Context Destroyed");
        try
        {
            scheduler.shutdown();
        }
        catch (SchedulerException e)
        {
            e.printStackTrace();
        }
    }
}
