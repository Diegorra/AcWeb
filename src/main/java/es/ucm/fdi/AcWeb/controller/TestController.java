package es.ucm.fdi.AcWeb.controller;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.Submission;
import es.ucm.fdi.ac.test.NCDTest;
import es.ucm.fdi.ac.test.Test;
import es.ucm.fdi.util.archive.ZipFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;

@Controller
@RequestMapping("test")
public class TestController {

    Analysis ac = new Analysis();
    @Autowired
    private EntityManager entityManager;

    //En un futuro 2 funciones -> loadSources: carga ficheros y persiste info (POST)
    //                         -> runtTest: crea y corre el test y muestra el resultado (GET)
    @GetMapping("/{nameDir}")
    //http://localhost:8080/test/sample_aa
    public String loadSources_runTest(@PathVariable String nameDir, Model model) throws IOException {
        /** CARGAMOS LOS SOURCES QUE SE VAN A USAR PARA EL ANÁLISIS **/
        File file = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + nameDir);
        SourceSet sourceset = new SourceSet(file);
        ac.loadSources(sourceset);

        /** CREAMOS Y CORREMOS EL TEST **/
        Test t =  new NCDTest(new ZipFormat());
        ac.prepareTest(t);
        ac.applyTest(t);

        //Persistir
        //entityManager.persist(ac);

        //Enviar al modelo
        ArrayList<Object> matrix = new ArrayList<Object>();
        for(Submission sb : ac.getSubmissions()){
            matrix.add(sb.getData(t.getTestKey()));
        }
        /**Prueba vista
        Double m[][] = new Double[50][50];
        double k = 0.1;
        for(int i=0; i < 50; i++){
            for(int j=0; j < 50; j++){
                m[i][j] = k*j;
            }
        }
        model.addAttribute("resultT", m);
        **/

        model.addAttribute("resultT", matrix);
        return "showResultTest";
    }

    @GetMapping
    public String main(){
        return "main";
    }
}
