// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@EnableAutoConfiguration
@SpringBootApplication
public class SampleSqliteApplication
{
    public static ApplicationContext ctx;
    
    public static Object getBean(final String name) {
        return SampleSqliteApplication.ctx.getBean(name);
    }
    
    public static void main(final String[] args) {
    	try {
    		 SampleSqliteApplication.ctx = SpringApplication.run(SampleSqliteApplication.class, args);
    	       //final ApplicationHome home = new ApplicationHome((Class)SampleSqliteApplication.class);
    	       /* final LicenseManager licenseManager = new LicenseManager();
    	        licenseManager.checkLicense();*/
    	       /* ApplicationConfig.init();
    	        System.out.println("ok");*/
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
       
    }
}
