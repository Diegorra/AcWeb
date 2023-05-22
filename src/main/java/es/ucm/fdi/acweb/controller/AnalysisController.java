package es.ucm.fdi.acweb.controller;

import com.fasterxml.jackson.databind.JsonNode;
import es.ucm.fdi.acweb.LocalData;
import es.ucm.fdi.acweb.Mapper;
import es.ucm.fdi.acweb.PdfService;
import es.ucm.fdi.acweb.ZipFileManager;
import es.ucm.fdi.acweb.model.*;
import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.extract.FileTreeModel;
import es.ucm.fdi.ac.extract.FileTreeNode;
import es.ucm.fdi.ac.parser.AntlrTokenizerFactory;
import es.ucm.fdi.ac.test.NCDTest;
import es.ucm.fdi.ac.test.Test;
import es.ucm.fdi.ac.test.TokenizingTest;
import es.ucm.fdi.util.archive.ZipFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.context.Context;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.*;

import java.util.*;
import java.util.List;

import static org.apache.commons.io.FileUtils.copyDirectory;


@Controller
@RequestMapping("analysis")
public class AnalysisController {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    @Autowired
    private Mapper map;

    @Autowired
    private ZipFileManager zfx; // Servico para gestioar zip y unzip

    @Autowired
    private PdfService pdfService;
    private static final Logger log = LogManager.getLogger(AnalysisController.class);

    Map<String, String> naming = new HashMap<>(); // Gestiona nombres de submission clave -> id_cv, valor nombre integrantes de submission


    /**
     * Exception to use when denying access to unauthorized users.

     */
    @ResponseStatus(
            value=HttpStatus.FORBIDDEN,
            reason="Debes estar registrado en el sistema para acceder")  // 403
    public static class NoEsTuPerfilException extends RuntimeException {}

    /**
     * Checks if user trying to access endpoint is authorised to do it
     */
    private void isAuthorised(HttpSession session, Long id){
        User requester = (User)session.getAttribute("u");
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        if (!requester.getId().equals(analysis.getOwner().getId())) {
            throw new NoEsTuPerfilException();
        }
    }

    /**
     * Removes filtered sources
     */
    @Transactional
    public void cleanAnalysis(long id){
        AnalysisWeb analysisWeb = entityManager.find(AnalysisWeb.class, id);

        entityManager.remove(analysisWeb.getSourceSet());

        for(SubmissionWeb sub : analysisWeb.getSubs()){
            entityManager.remove(sub);
        }

        analysisWeb.setAppliedTestKey(new ArrayList<>());

        analysisWeb.setSubs(new ArrayList<>());
        analysisWeb.setAppliedTestKey(new ArrayList<>());

        entityManager.merge(analysisWeb);

    }

    /**
     * Loads analysis given dir with sources
     */
    @Transactional
    public AnalysisWeb load(File sourcesDir, long id, HttpSession session) throws IOException {
        // Look deeper until more than 1 child
        while (sourcesDir.listFiles().length == 1) {
            sourcesDir = sourcesDir.listFiles()[0];
        }

        // Load Sources
        FileTreeModel ftm = new FileTreeModel();
        for (File root : sourcesDir.listFiles()) {
            log.info("Adding root for {}: {}", sourcesDir, root.getAbsolutePath());
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

        analysis.fromAc(entityManager.find(User.class, requester.getId()), ssw, submissionWebs, analysis.getName());

        //Una vez se contruye el análisis
        for(SubmissionWeb submissionWeb : analysis.getSubs()){
            submissionWeb.setNames(naming);
        }

        entityManager.persist(analysis);
        log.info("Analysis {} persisted", analysis.getName());

        return analysis;
    }


    // ------------------------------------------------------- ANALYSIS -------------------------------------------------------

    /**
     * Creates a new analysis
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

    /**
     * Loads an existing analysis
     */
    @GetMapping("/{id}")
    @Transactional
    public String getAnalysis(@PathVariable long id, Model model, HttpSession session){
        isAuthorised(session, id);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        model.addAttribute("analysis", analysis);
        return "mainView";
    }

    @DeleteMapping("/{id}/remove")
    @Transactional
    @ResponseBody
    public String removeAnalysis(@PathVariable long id, HttpSession session){
        isAuthorised(session, id);
        AnalysisWeb analysisWeb = entityManager.find(AnalysisWeb.class, id);
        entityManager.remove(analysisWeb);
        return "{}";
    }

    /**
     * Download files linked to analysis
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Object> download(@PathVariable long id, HttpSession session) throws IOException {
        isAuthorised(session, id);
        // Obtener la ruta del directorio
        String directoryName = localData.getFolder("analysis/" + id).toString();
        Path directoryPath = Paths.get(directoryName);

        // Configurar los encabezados de la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + directoryName + ".zip\"");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // Crear un archivo ZIP temporal
        Path tempZipFile = zfx.compressFile(directoryPath);

        // Leer el contenido del archivo ZIP temporal en un array de bytes
        byte[] zipData = Files.readAllBytes(tempZipFile);
        // Eliminar el archivo ZIP temporal del disco
        Files.delete(tempZipFile);

        // Devolver la respuesta HTTP con los encabezados y el contenido del archivo ZIP
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(zipData);
    }

    // ------------------------------------------------------- FILTER -------------------------------------------------------

    /**
     * Filter sources given a String with the filter to apply
     */
    @PostMapping("/{id}/newFilter")
    @Transactional
    public String addFilter(@PathVariable long id, @RequestParam("filters") String newFilter, Model model, HttpSession session) throws IOException {
        isAuthorised(session, id);
        //Obtenemos filtros previos y añadimos el nuevo
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        List<String> filters = analysis.getFilters();
        filters.add(newFilter);

        cleanAnalysis(id);
        loadFilter(id, filters, session);

        model.addAttribute("analysis", analysis);
        return "mainView";
    }

    /**
     * Removes given filter from the filter list
     */
    @DeleteMapping("/{id}/removeFilter")
    @Transactional
    @ResponseBody
    public ResponseEntity<String> removeFilter(@PathVariable long id, @RequestBody JsonNode data, Model model, HttpSession session) throws IOException {
        isAuthorised(session, id);
        List<String> filters = entityManager.find(AnalysisWeb.class, id).getFilters();
        filters.remove(data.get("filter").asText());
        cleanAnalysis(id);
        loadFilter(id, filters, session);

        return ResponseEntity.ok("{}");
    }

    /**
     * Given list of filters, applies filter to sources
     */
    @Transactional
    public void loadFilter(long id, List<String> filters, HttpSession session) throws IOException {
        File srcDir = localData.getFolder("analysis/" + id + "/rawInput");
        File targetDir = localData.getFolder("analysis/" + id + "/filterInput");

        //Si targetDir existe borramos para poder volver a aplicar filtros
        if(Files.exists(targetDir.toPath()) && targetDir.listFiles().length >= 1){
            FileSystemUtils.deleteRecursively(targetDir);
        }

        //Copiamos los contenidos de rawInput a filterInput
        try {
            copyDirectory(srcDir, targetDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Creamos un array con las extensiones que están permitidas en todos los test activos
        ArrayList<String> allowedExtensions = new ArrayList<>();
        for(String filter : filters){
            ArrayList<String> i = new ArrayList<>(Arrays.asList(filter.split(",")));
            allowedExtensions.addAll(i);
        }

        //recorremos directorio y chequeamos que cumple filtros siempre que haya, sino borramos
        if(!filters.isEmpty()){
            zfx.clean(targetDir.listFiles(), allowedExtensions);
        }

        AnalysisWeb analysis = load(targetDir, id, session);
        analysis.setFilters(filters);

        entityManager.persist(analysis);
    }

    // ------------------------------------------------------- SOURCES -------------------------------------------------------

    /**
     * Receives a zip, unzips it, and creates a source-set that is ready for
     * analysis
     */
    @PostMapping("/{id}/loadSources")
    @Transactional
    public String loadSources(@PathVariable long id, @RequestParam("file") MultipartFile rootFile, Model model, HttpSession session) throws IOException {
        isAuthorised(session, id);
        /** Build structure of SourceSet **/
        // Unzip folder with sources
        File targetDir = localData.getFolder("analysis/" + id + "/rawInput");
        naming.clear();
        zfx.extractZip(rootFile, targetDir.toPath(), naming);

        AnalysisWeb analysis = load(targetDir, id, session);
        analysis.setName(rootFile.getOriginalFilename().replaceAll("\\.zip$", ""));


        entityManager.persist(analysis);

        model.addAttribute("analysis", analysis);
        return "redirect:/analysis/" + analysis.getId();
        //return "Everything ok";
    }

    /**
     * Get all sources of analysis to display tree view
     */
    @GetMapping("/{id}/getSources")
    @ResponseBody
    public ArrayList<FileTreeNodeWeb.Transfer> getSources(@PathVariable long id, HttpSession session){
        isAuthorised(session, id);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        FileTreeNodeWeb.Transfer ftnwT = analysis.getSourceSet().getSourceRoots().toTransfer();
        return ftnwT.getNodes();
    }

    /**
     * Gets two codes of sources for comparison
     */
    @GetMapping("/{analysisId}/get/{id1}/{id2}")
    public String getCodeComparison(@PathVariable long analysisId, @PathVariable String id1, @PathVariable String id2, Model model, HttpSession session){
        isAuthorised(session, analysisId);
        SubmissionWeb sub1 = entityManager.createNamedQuery("SubmissionWeb.byIdAuthors", SubmissionWeb.class)
                .setParameter("id", id1)
                .setParameter("analysisId", analysisId)
                .getSingleResult();
        SubmissionWeb sub2 = entityManager.createNamedQuery("SubmissionWeb.byIdAuthors", SubmissionWeb.class)
                .setParameter("id", id2)
                .setParameter("analysisId", analysisId)
                .getSingleResult();

        ArrayList<String> sources1 = new ArrayList<>();
        for(SourceWeb sw1 : sub1.getSources()){
            sources1.add(sw1.toTransfer().getFileName());
        }

        ArrayList<String> sources2 = new ArrayList<>();
        for(SourceWeb sw2 : sub2.getSources()){
            sources2.add(sw2.toTransfer().getFileName());
        }

        model.addAttribute("analysis", analysisId);
        model.addAttribute("sub1", id1);
        model.addAttribute("sub2", id2);
        model.addAttribute("files1", sources1);
        model.addAttribute("files2", sources2);

        return "codeComparison";
    }

    /**
     * Gets two codes of sources for comparison
     */
    @GetMapping("/{analysisId}/getFile/{name}/{file}")
    public ResponseEntity<Map<String, String>> getCodeOfFile(@PathVariable long analysisId, @PathVariable String name, @PathVariable String file, Model model, HttpSession session){
        isAuthorised(session, analysisId);
        SubmissionWeb sub = entityManager.createNamedQuery("SubmissionWeb.byIdAuthors", SubmissionWeb.class)
                .setParameter("id", name)
                .setParameter("analysisId", analysisId)
                .getSingleResult();

        SourceWeb source = entityManager.createNamedQuery("SourceWeb.byFileName", SourceWeb.class)
                .setParameter("file", file)
                .setParameter("id", sub.getId())
                .getSingleResult();

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("id", sub.getAnotations() + ": " + source.getFileName());
        responseMap.put("code1", source.getCode());

        return ResponseEntity.ok(responseMap);
    }

    // ------------------------------------------------------- TEST -------------------------------------------------------

    /**
     * Applies specified test to current analysis
     */
    @GetMapping("/{id}/{testKey}")
    @Transactional
    public String runTest(@PathVariable long id, @PathVariable String testKey, Model model, HttpSession session) throws IOException {
        isAuthorised(session, id);
        /** Load analysis from BD, convert it to ac and run test **/
        // Load analysis from BD, cast it to ac
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);

        if(!analysis.getAppliedTestKey().contains(testKey)){
            // Vemos si hay filtros aplicado para correr el test sobre los mismo
            File targetDir = localData.getFolder("analysis/" + id + "/filterInput");
            if(!Files.exists(targetDir.toPath()) || targetDir.listFiles().length < 1){
                targetDir = localData.getFolder("analysis/" + id + "/rawInput");
            }
            Analysis ac = analysis.analysisToAc(targetDir);

            // Prepare tokenization
            Analysis.setTokenizerFactory(new AntlrTokenizerFactory());
            Test test = new NCDTest(new ZipFormat());
            /*switch (testKey){
                default:
                    test = new NCDTest(new ZipFormat());

            }*/


            if (test instanceof TokenizingTest) {
                ((TokenizingTest) test).setTokenizer(ac.chooseTokenizer());
            }

            // Create and run Test
            ac.prepareTest(test);
            ac.applyTest(test);

            /**  Persistence **/
            ArrayList<String> keys = new ArrayList<>();
            keys.add(testKey);
            map.persistData(analysis, ac, keys);
        }


        /** Enviar al modelo **/
        ArrayList<List<Float>> matrix = new ArrayList<>();
        for(SubmissionWeb sb : analysis.getSubs()){
            for(TestResultWeb rs : sb.getData()){
                matrix.add(rs.getResult());
            }
        }

        model.addAttribute("analysis", analysis);
        model.addAttribute("result", matrix);
        return "mainView";

    }

    // ------------------------------------------------------- VISUALIZATIONS -------------------------------------------------------

    /**
     * Loads main histogram from testResult
     */
    @GetMapping("/{id}/histogram")
    @ResponseBody
    public List<AnalysisWeb.DataPoint> getHistogram(@PathVariable long id, HttpSession session) {
        isAuthorised(session, id);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        return analysis.toPoints();
    }

    /**
     * Loads histogram of submission given it idAuthors from testResult
     */
    @GetMapping("/{analysisId}/histogramOf/{sourceId}")
    @ResponseBody
    public List<AnalysisWeb.DataPoint> getHistogramOfSource(@PathVariable long analysisId,  @PathVariable String sourceId, HttpSession session){
        isAuthorised(session, analysisId);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, analysisId);
        SubmissionWeb sub = entityManager.createNamedQuery("SubmissionWeb.byIdAuthors", SubmissionWeb.class)
                .setParameter("id", sourceId)
                .setParameter("analysisId", analysisId)
                .getSingleResult();
        return analysis.toPointsGivenSub(analysis.getSubs().indexOf(sub));
    }

    /**
     * Loads every histogram of each submission from testResult
     */
    @GetMapping("/{id}/getAllHistograms")
    @ResponseBody
    public List<List<Float>> getAllHistograms(@PathVariable long id, HttpSession session){
        isAuthorised(session, id);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        ArrayList<List<Float>> matrix = new ArrayList<>();
        for(SubmissionWeb sb : analysis.getSubs()){
            for(TestResultWeb rs : sb.getData()){
                matrix.add(rs.getResult());
            }
        }
        return matrix;
    }

    /**
     * Given analysis id and JsonNode containing d3 data, downloads report.
     */
    @PostMapping("/{id}/downloadReport")
    @ResponseBody
    public ResponseEntity<byte[]> downloadReport(@PathVariable long id, @RequestBody JsonNode data, HttpSession session) throws Exception {
        isAuthorised(session, id);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);

        // Definir contexto para renderizar plantilla de reporte
        Context context = new Context();
        context.setVariable("analysis", analysis);
        context.setVariable("histogram", data.get("svg").asText());


        String html = pdfService.createHtmlFromTemplate("report", context);
        File report = new File(localData.getFolder("analysis/" + id), "report.pdf"); // Crear el fichero pdf
        pdfService.createPdf(html, report.getPath()); // Cargar el contenido al pdf

        // Configurar los encabezados de la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        // Devolver la respuesta HTTP con los encabezados y el contenido del informe
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(Files.readAllBytes(report.toPath())); // Devolver como flujo de bytes el archivo pdf para su descarca
    }


}

