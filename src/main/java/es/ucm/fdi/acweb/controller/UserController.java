package es.ucm.fdi.acweb.controller;

import es.ucm.fdi.acweb.model.AnalysisWeb;
import es.ucm.fdi.acweb.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.util.List;


@Controller
public class UserController {

    @Autowired
    private EntityManager entityManager;
    private static final Logger log = LogManager.getLogger(UserController.class);
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


}
