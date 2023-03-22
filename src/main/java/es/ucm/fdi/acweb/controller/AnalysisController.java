package es.ucm.fdi.acweb.controller;

import es.ucm.fdi.acweb.LocalData;
import es.ucm.fdi.acweb.Mapper;
import es.ucm.fdi.acweb.model.*;
import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.Submission;
import es.ucm.fdi.ac.extract.FileTreeModel;
import es.ucm.fdi.ac.extract.FileTreeNode;
import es.ucm.fdi.ac.parser.AntlrTokenizerFactory;
import es.ucm.fdi.ac.test.NCDTest;
import es.ucm.fdi.ac.test.Test;
import es.ucm.fdi.ac.test.TokenizingTest;
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

    //private Analysis ac; // de forma temporal hasta tener persistencia

    @Autowired
    private Mapper map;

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
    public String loadSources(@PathVariable long id, @RequestParam("file") MultipartFile rootFile, Model model, HttpSession session) throws IOException {
        /** Build structure of SourceSet **/
        //Unzip folder with sources
        File targetDir = localData.getFolder("analysis/" + id);
        unzip(rootFile, targetDir.toPath());

        // Look deeper until more than 1 child
        while (targetDir.listFiles().length == 1) {
            targetDir = targetDir.listFiles()[0];
        }

        // Load Sources
        FileTreeModel ftm = new FileTreeModel();
        for (File root : targetDir.listFiles()) {
            log.info("Adding root for {}: {}", targetDir, root.getAbsolutePath());
            ftm.addSource(root);
        }


        /** Initialise ac analysis **/
        Analysis ac = new Analysis();
        SourceSet ss = new SourceSet((FileTreeNode) ftm.getRoot());
        ac.loadSources(ss);

        /** Cast to web, persist the result **/
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        User requester = (User)session.getAttribute("u");
        SourceSetWeb ssw = map.getSourceSetWeb(ss, analysis);
        ArrayList<SubmissionWeb> submissionWebs = map.getSubmissions(ac, analysis);

        //SourceSetWeb ssw = sourceSetFromAc(ss, analysis);
        //analysis.analysisFromAc(ac, entityManager.find(User.class, requester.getId()), ssw, rootFile.getName());
        analysis.fromAc(entityManager.find(User.class, requester.getId()), ssw, submissionWebs, rootFile.getOriginalFilename());
        entityManager.persist(analysis);
        log.info("Analysis {} persisted", rootFile.getOriginalFilename());

        model.addAttribute("analysis", analysis);
        return "redirect:/analysis/" + analysis.getId();
    }

    @GetMapping("/{id}/test")
    @Transactional
    public String runTest(@PathVariable long id, Model model) throws IOException {
        /** Load analysis from BD, convert it to ac and run test **/
        //Load analysis from BD, cast it to ac
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        Analysis ac = analysis.analysisToAc(localData.getFolder("analysis/" + id));

        // prepare tokenization
        Analysis.setTokenizerFactory(new AntlrTokenizerFactory());
        Test test = new NCDTest(new ZipFormat());


        if (test instanceof TokenizingTest) {
            ((TokenizingTest) test).setTokenizer(ac.chooseTokenizer());
        }

        // Create and run Test
        ac.prepareTest(test);
        ac.applyTest(test);

        /**  Persistence **/
        ArrayList<String> keys = new ArrayList<>();
        keys.add("Zip_ncd_sim");
        map.persistData(analysis, ac, keys);
        //analysis.persistData(ac, keys);
        //entityManager.persist(analysis);

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
        /*int[][] matrix = new int[3][3];

        int value = 1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix[i][j] = value;
                value++;
            }
        }*/

        model.addAttribute("analysis", analysis);
        model.addAttribute("result", matrix);
        return "mainView";

    }

    /**
     * Creates a new analysis from a button on main view
     *
     */
    @GetMapping
    @Transactional
    public String newAnalysis(Model model, HttpSession session) {
        AnalysisWeb analysis = new AnalysisWeb();
        User requester = (User)session.getAttribute("u");
        analysis.setOwner(entityManager.find(User.class, requester.getId()));
        analysis.setName("new_analysis");
        entityManager.persist(analysis);
        entityManager.flush();

        model.addAttribute("analysis", analysis);
        return "mainView";
    }

    @GetMapping("/{id}")
    @Transactional
    public String loadAnalysis(@PathVariable long id, Model model){
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        model.addAttribute("analysis", analysis);
        return "mainView";
    }

}

