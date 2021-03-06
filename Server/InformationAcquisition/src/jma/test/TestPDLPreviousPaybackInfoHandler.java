package jma.test;

import jma.Configuration;
import jma.RetryRequiredException;
import jma.handlers.CalculatePDLPreviousPaybackInfoHandler;
import thirdparty.config.YLZHConfiguration;
import catfish.base.Logger;
import catfish.base.StartupConfig;
import catfish.base.httpclient.HttpClientConfig;
import catfish.base.persistence.queue.PersistenceConfig;

public class TestPDLPreviousPaybackInfoHandler {

    public static void main(String[] args) {
        StartupConfig.initialize();
        Logger.initialize();
        Configuration.readConfiguration();
        YLZHConfiguration.readConfiguration();
        HttpClientConfig.initialize();
        PersistenceConfig.initialize();
        
        String appId = "68B08508-6654-E511-87D5-80DB2F14945F";
        
        CalculatePDLPreviousPaybackInfoHandler handler = new CalculatePDLPreviousPaybackInfoHandler();
        try {
            handler.execute(appId);
            //handler.execute(args[0]);
        } catch (RetryRequiredException e) {
            e.printStackTrace();
        }
    }

}
