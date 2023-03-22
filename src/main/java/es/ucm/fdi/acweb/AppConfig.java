package es.ucm.fdi.acweb;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * General configuration for a Spring app.
 * 
 * Declares multiple beans (which can later be accessed anywhere) via
 * Spring magic.
 */
@Configuration
public class AppConfig {

    @Autowired
    private Environment env;

    /**
     * Declares a LocalData bean.
     * 
     * This allows you to write, in any part of Spring-managed code,
     * `@Autowired LocalData localData`, and have it initialized
     * with the result of this method.
     */
    @Bean(name = "localData")
    public LocalData getLocalData() {
        return new LocalData(new File(env.getProperty("es.ucm.fdi.AcWeb.base-path")));
    }
}