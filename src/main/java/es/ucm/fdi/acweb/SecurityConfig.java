package es.ucm.fdi.acweb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private Environment env;

    private static final Logger log = LogManager.getLogger(SecurityConfig.class);

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        String debugProperty = env.getProperty("es.ucm.fdi.AcWeb.debug");
        log.info("Debug property is : {}", debugProperty);
        boolean allowH2Console = (debugProperty != null && Boolean.parseBoolean(debugProperty.toLowerCase()));

        WebSecurityCustomizer withH2 = (web) -> web.ignoring().antMatchers("/h2/**", "/js/**");
        WebSecurityCustomizer withoutH2 = (web) -> web.ignoring().antMatchers("/js/**");

        return allowH2Console ? withH2 : withoutH2;
    }

    /**
     * Main security configuration.
     *
     * The first rule that matches will be followed - so if a rule decides to grant
     * access
     * to a resource, a later rule cannot deny that access, and vice-versa.
     *
     * To disable security entirely, just add an .antMatchers("**").permitAll()
     * as a first rule. Note that this may break an application that expects to have
     * login information available.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf().ignoringAntMatchers("/api/**")
                .and()
                .authorizeRequests()
                .antMatchers("/register").permitAll()
                .antMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll().successHandler(loginSuccessHandler);
        http.headers().frameOptions().sameOrigin();

        return http.build();
    }

    /**
     * Declares a PasswordEncoder bean.
     *
     * This allows you to write, in any part of Spring-managed code,
     * `@Autowired PasswordEncoder passwordEncoder`, and have it initialized
     * with the result of this method.
     */
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        // by default in Spring Security 5, a wrapped new BCryptPasswordEncoder();
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Declares a springDataUserDetailsService bean.
     *
     * This is used to translate from Spring Security users to in-application users.
     */
    @Bean
    public AcUserDetailsService UserDetailsService() {
        return new AcUserDetailsService();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;
}
