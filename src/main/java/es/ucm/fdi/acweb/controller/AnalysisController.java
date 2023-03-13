package es.ucm.fdi.acweb.controller;

import es.ucm.fdi.acweb.LocalData;
import es.ucm.fdi.acweb.Mapper;
import es.ucm.fdi.acweb.model.AnalysisWeb;
import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.Submission;
import es.ucm.fdi.ac.extract.FileTreeModel;
import es.ucm.fdi.ac.extract.FileTreeNode;
import es.ucm.fdi.ac.parser.AntlrTokenizerFactory;
import es.ucm.fdi.ac.test.NCDTest;
import es.ucm.fdi.ac.test.Test;
import es.ucm.fdi.ac.test.TokenizingTest;
import es.ucm.fdi.acweb.model.User;
import es.ucm.fdi.util.archive.ZipFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Controller
@RequestMapping("analysis")
public class AnalysisController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    private Analysis ac; // de forma temporal hasta tener persistencia

    private static final Logger log = LogManager.getLogger(AnalysisController.class);

    /** Función auxiliar que descomprime el fichero de entrada en ./data **/
    public void unzip(MultipartFile file, Path targetPath) throws IOException {
        try (ZipInputStream inputStream = new ZipInputStream(file.getInputStream())) {
            for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null;) {
                Path resolvedPath = targetPath.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(inputStream, resolvedPath);
                } else {
                    Files.createDirectories(resolvedPath);
                }
            }
        } catch (IOException ioe) {
            log.warn("Error unzipping uploaded file to " + targetPath, ioe);
        }
    }

    /**
     * Receives a zip, unzips it, and creates a source-set that is ready for
     * analysis
     */
    @PostMapping("/{id}/sources")
    @Transactional
    //http://localhost:8080/sources
    public String loadSources(@PathVariable long id, @RequestParam("file") MultipartFile rootFile, Model model) throws IOException {
        File targetDir = localData.getFolder("analysis/" + id);
        unzip(rootFile, targetDir.toPath());

        // Look deeper until more than 1 child
        while (targetDir.listFiles().length == 1) {
            targetDir = targetDir.listFiles()[0];
        }

        /** Load Sources **/
        FileTreeModel ftm = new FileTreeModel();
        for (File root : targetDir.listFiles()) {
            //log.info("Adding root for {}: {}", targetDir, root.getAbsolutePath());
            ftm.addSource(root);
        }

        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);

        ac = new Analysis(); //Analysis ac = analysis.castToAc2();
        SourceSet ss = new SourceSet((FileTreeNode) ftm.getRoot());
        ac.loadSources(ss);


        //Convertimos con mapper ac a AnalysisEntity
        //analysis.castToAcWeb(ac);
        //entityManager.persist(analysis);
        model.addAttribute("analysis", analysis);
        return "main";
    }

    @GetMapping("/{id}/test")
    @Transactional
    public String runTest(@PathVariable long id, Model model){
        /** Load analysis from BD and convert to ac2 **/
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        //Analysis ac = analysis.castToAc2();

        // prepare tokenization
        Analysis.setTokenizerFactory(new AntlrTokenizerFactory());
        Test test = new NCDTest(new ZipFormat());


        if (test instanceof TokenizingTest) {
            ((TokenizingTest) test).setTokenizer(ac.chooseTokenizer());
        }

        // Create and run Test
        ac.prepareTest(test);
        ac.applyTest(test);

        /**  Persistance **/
        //analysis.castToAcWeb(ac);

        /** Report results and send back to model **/
        ArrayList<Object> matrix = new ArrayList<>();
        for (Submission sb : ac.getSubmissions()) {
            Object o = sb.getData("Zip_ncd_sim");
            matrix.add(o);
        }

        /**
         * Enviar al modelo
         * //inicialmente resultados se cogeran de objeto submission a partir del
         * AnalysisEntity persistido
         * ArrayList<ArrayList<Float>> matrix = new ArrayList<>();
         * for(SubmissionEntity sb : analysis.getSubs()){
         * for(TestResultEntity rs : sb.getData()){
         * matrix.add((ArrayList<Float>) rs.getResult());
         * }
         * }
         **/

        model.addAttribute("result", matrix);
        return "showResultTest";

    }

    /**
     * Creates a new analysis from a button on main view
     *
     */
    @GetMapping
    @Transactional
    public String newAnalysis(Model model, HttpSession session) {
        //Creamos nuevo análisis
        AnalysisWeb analysis = new AnalysisWeb();
        User requester = (User)session.getAttribute("u");
        analysis.setOwner(entityManager.find(User.class, requester.getId()));
        entityManager.persist(analysis);
        entityManager.flush();

        model.addAttribute("analysis", analysis);
        return "main";
    }

    @GetMapping("/{id}")
    @Transactional
    public String loadAnalysis(@PathVariable long id, Model model){
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        model.addAttribute("analysis", analysis);
        return "main";
    }

}

