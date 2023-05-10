package es.ucm.fdi.acweb.controller;

import es.ucm.fdi.acweb.model.AnalysisWeb;
import es.ucm.fdi.acweb.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.List;


@Controller
public class UserController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger log = LogManager.getLogger(UserController.class);

    /**
     * Returns main page of user
     */
    @GetMapping
    @Transactional
    public String startingPage(Model model, HttpSession session){
        User requester = (User)session.getAttribute("u");
        requester = entityManager.find(requester.getClass(), requester.getId());
        boolean load = false;
        if(!requester.getAnalysesWeb().isEmpty()) {
            List<AnalysisWeb> previousAnalysis = requester.getAnalysesWeb();
            model.addAttribute("previousAnalysis", previousAnalysis);
            log.info("Loading analysis from {}", requester.getUsername());
            load = true;
        }
        model.addAttribute("load", load);
        return "startingPage";
    }


    @GetMapping("/login")
    public String login() {
        return "/forms/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register(Model model){
        User newUser = new User();
        model.addAttribute("newUser", newUser);
        return "/forms/register";
    }

    @PostMapping("/register")
    @Transactional
    public String register(Model model, @ModelAttribute("newUser") User newUser){
        String userName = newUser.getUsername();

        Long exists =((Number) entityManager
                .createNamedQuery("User.hasUsername")
                .setParameter("username", userName)
                .getSingleResult())
                .longValue();

        if(exists == 0){
            newUser.setRoles("USER");
            newUser.setPasswd(passwordEncoder.encode(newUser.getPasswd()));
            newUser.setEnabled(true);
            entityManager.persist(newUser);
        }
        else{
            model.addAttribute("error", "Nombre de usuario existente");
            return "/forms/register";
        }
        return "redirect:/login";
    }

}
