package es.ucm.fdi.acweb.controller;

import es.ucm.fdi.acweb.LocalData;
import es.ucm.fdi.acweb.Mapper;
import es.ucm.fdi.acweb.ZipFileExtractor;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.*;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


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

    ZipFileExtractor zfx = new ZipFileExtractor();

    private static final Logger log = LogManager.getLogger(AnalysisController.class);

    /**
     * Exception to use when denying access to unauthorized users.
     *
     * In general, admins are always authorized, but users cannot modify
     * each other's profiles.
     */
    @ResponseStatus(
            value=HttpStatus.FORBIDDEN,
            reason="No eres administrador, y éste no es tu perfil")  // 403
    public static class NoEsTuPerfilException extends RuntimeException {}

    // check permissions
    private boolean isAuthorised(HttpSession session, Long id){
        User requester = (User)session.getAttribute("u");
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        if (!requester.getId().equals(analysis.getOwner().getId())) {
            throw new NoEsTuPerfilException();
        }
        return true;
    }

    /**
     * Receives a zip, unzips it, and creates a source-set that is ready for
     * analysis
     */
    //  @ResponseBody
    @PostMapping("/{id}/sources")
    @Transactional
    //http://localhost:8080/sources
    public String loadSources(@PathVariable long id, @RequestParam("file") MultipartFile rootFile, Model model, HttpSession session) throws IOException {
        isAuthorised(session, id);
        /** Build structure of SourceSet **/
        // Unzip folder with sources
        File targetDir = localData.getFolder("analysis/" + id);
        zfx.extractZip(rootFile, targetDir.toPath());

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

        analysis.fromAc(entityManager.find(User.class, requester.getId()), ssw, submissionWebs, rootFile.getOriginalFilename().replaceAll("\\.zip$", ""));
        entityManager.persist(analysis);
        log.info("Analysis {} persisted", rootFile.getOriginalFilename());

        model.addAttribute("analysis", analysis);
        return "redirect:/analysis/" + analysis.getId();
        //return "Everything ok";
    }

    @GetMapping("/{id}/{testKey}")
    @Transactional
    public String runTest(@PathVariable long id, @PathVariable String testKey, Model model, HttpSession session) throws IOException {
        isAuthorised(session, id);
        /** Load analysis from BD, convert it to ac and run test **/
        // Load analysis from BD, cast it to ac
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);

        if(!analysis.getAppliedTestKey().contains(testKey)){
            Analysis ac = analysis.analysisToAc(localData.getFolder("analysis/" + id));

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

    /**
     * Loads an existing analysis
     */
    @GetMapping("/{id}")
    @Transactional
    public String loadAnalysis(@PathVariable long id, Model model, HttpSession session){
        isAuthorised(session, id);
        AnalysisWeb analysis = entityManager.find(AnalysisWeb.class, id);
        model.addAttribute("analysis", analysis);
        return "mainView";
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
    @GetMapping("/{analysisId}/get/{id1}/{id2}/{index}")
    public String getCodeComparison(@PathVariable long analysisId, @PathVariable int id1, @PathVariable int id2, @PathVariable int index, Model model, HttpSession session){
        isAuthorised(session, analysisId);
        SubmissionWeb sub1 = entityManager.createNamedQuery("SubmissionWeb.byInternalId", SubmissionWeb.class)
                .setParameter("id", id1)
                .setParameter("analysisId", analysisId)
                .getSingleResult();
        SubmissionWeb sub2 = entityManager.createNamedQuery("SubmissionWeb.byInternalId", SubmissionWeb.class)
                .setParameter("id", id2)
                .setParameter("analysisId", analysisId)
                .getSingleResult();

        //Una vez tienes los dos submissions -> creas un array con ficheros que coinciden
        ArrayList<SourceWeb.Transfer> sources1 = new ArrayList<>();
        ArrayList<SourceWeb.Transfer> sources2 = new ArrayList<>();

        for(SourceWeb sw1 : sub1.getSourceRoots()){
            for(SourceWeb sw2 : sub2.getSourceRoots()){
                if(sw1.getFileName().equals(sw2.getFileName())){
                    sources1.add(sw1.toTransfer());
                    log.info("Coinciden en : " + sw1.getFileName());
                    sources2.add(sw2.toTransfer());
                }
            }
        }
        model.addAttribute("length", sources1.size());
        model.addAttribute("index", index);

        model.addAttribute("id1", sub1.getId_authors() + ": " + sources1.get(index).getFileName());
        model.addAttribute("source1", sources1.get(index).getCode());

        model.addAttribute("id2", sub2.getId_authors()+ ": " + sources2.get(index).getFileName());
        model.addAttribute("source2", sources2.get(index).getCode());
        log.info("out of function");
        return "codeComparison";
    }

    @GetMapping("/{analysisId}/getFile/{name}/{file}")
    public String getCodeOfFile(@PathVariable long analysisId, @PathVariable String name, @PathVariable String file, Model model, HttpSession session){
        isAuthorised(session, analysisId);
        SubmissionWeb sub = entityManager.createNamedQuery("SubmissionWeb.byIdAuthors", SubmissionWeb.class)
                .setParameter("id", name)
                .setParameter("analysisId", analysisId)
                .getSingleResult();

        SourceWeb source = entityManager.createNamedQuery("SourceWeb.byFileName", SourceWeb.class)
                .setParameter("file", file)
                .setParameter("id", sub.getId())
                .getSingleResult();

        model.addAttribute("id1", sub.getId_authors() + ": " + source.getFileName());
        model.addAttribute("code1", source.getCode());
        return "codeOfFile";
    }

    @GetMapping("/download/{id}")
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
        Path tempZipFile = Files.createTempFile("tempZip", ".zip");
        try (
                // Crear un OutputStream para escribir en el archivo ZIP temporal
                OutputStream outputStream = Files.newOutputStream(tempZipFile);
                // Crear un ZipOutputStream para comprimir los archivos en el ZIP
                ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)
        ) {
            // Recorrer el contenido del directorio y comprimirlo en el archivo ZIP
            Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Crear una entrada ZIP para el archivo actual
                    ZipEntry zipEntry = new ZipEntry(directoryPath.relativize(file).toString());
                    // Agregar la entrada ZIP al ZipOutputStream
                    zipOutputStream.putNextEntry(zipEntry);
                    // Copiar el contenido del archivo actual al ZipOutputStream
                    Files.copy(file, zipOutputStream);
                    // Cerrar la entrada ZIP
                    zipOutputStream.closeEntry();
                    // Continuar con el siguiente archivo
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        // Leer el contenido del archivo ZIP temporal en un array de bytes
        byte[] zipData = Files.readAllBytes(tempZipFile);
        // Eliminar el archivo ZIP temporal del disco
        Files.delete(tempZipFile);

        // Devolver la respuesta HTTP con los encabezados y el contenido del archivo ZIP
        return ResponseEntity.status(HttpStatus.OK)
                .headers(headers)
                .body(zipData);
    }
}

