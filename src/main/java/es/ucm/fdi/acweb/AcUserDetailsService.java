package es.ucm.fdi.acweb;

import es.ucm.fdi.acweb.model.User;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

/**
 * Authenticates login attempts against a JPA database
 */
public class AcUserDetailsService implements UserDetailsService {

    private static Logger log = LogManager.getLogger(AcUserDetailsService.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        this.entityManager = em;
    }


    public UserDetails loadUserByUsername(String username) {
        try {
            User u = entityManager.createNamedQuery("User.byUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            // build UserDetails object
            ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
            for (String r : u.getRoles().split("[,]")) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + r));
                log.info("Roles for " + username + " include " + roles.get(roles.size() - 1));
            }
            return new org.springframework.security.core.userdetails.User(
                    u.getUsername(), u.getPasswd(), roles);
        } catch (Exception e) {
            log.info("No such user: " + username + " (error = " + e.getMessage() + ")");
            throw new UsernameNotFoundException(username);
        }
    }
}