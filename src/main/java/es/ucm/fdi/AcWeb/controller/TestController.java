package es.ucm.fdi.AcWeb.controller;

import antlr.NameSpace;
import es.ucm.fdi.AcWeb.FilesStorageService;
import es.ucm.fdi.AcWeb.Mapper;
import es.ucm.fdi.AcWeb.model.AnalysisEntity;
import es.ucm.fdi.AcWeb.model.SubmissionEntity;
import es.ucm.fdi.AcWeb.model.TestResultEntity;
import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.Submission;
import es.ucm.fdi.ac.extract.FileTreeModel;
import es.ucm.fdi.ac.extract.FileTreeNode;
import es.ucm.fdi.ac.test.NCDTest;
import es.ucm.fdi.ac.test.Test;
import es.ucm.fdi.util.archive.ZipFormat;
import org.apache.tomcat.util.file.ConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

@Controller
@RequestMapping("test")
public class TestController {

    Mapper map= new Mapper();
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FilesStorageService storageService;


    /** Función auxiliar que descomprime el fichero de entrada en ./uploads **/
    public void unzip(MultipartFile file) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(file.getInputStream());
        Path path = Paths.get("./uploads/");
        for (ZipEntry entry; (entry = inputStream.getNextEntry()) != null; ) {
            Path resolvedPath = path.resolve(entry.getName());
            if (!entry.isDirectory()) {
                Files.createDirectories(resolvedPath.getParent());
                Files.copy(inputStream, resolvedPath);
            } else {
                Files.createDirectories(resolvedPath);
            }
        }
    }

    @PostMapping("/sources")
    //http://localhost:8080/test/sample_aa
    public String loadSources_runTest(@RequestParam("file") MultipartFile rootFile, Model model) throws IOException {
        /** Save file in local data **/
        unzip(rootFile);
        String name = rootFile.getOriginalFilename().replace(".zip", "");
        File file = ResourceUtils.getFile("/uploads/" + name);

        Collection<String> dir = Arrays.asList(file.list());


        /** Load Sources **/
        FileTreeModel ftm = new FileTreeModel();
        for(String dirName : dir){
            File root = new File("./uploads/" + name + "/" + dirName);
            if(root == null){
                throw new IOException("No such file: " + dirName);
            }
            if(!root.isDirectory()){
                throw new IOException("Not a directory: " + dirName);
            }

            for(File f : root.listFiles()){
                ftm.addSource(f);
            }
        }
        Analysis ac = new Analysis();
        SourceSet ss = new SourceSet((FileTreeNode) ftm.getRoot());
        ac.loadSources(ss);

        /** Create and run Test **/
        Test t =  new NCDTest(new ZipFormat());
        ac.prepareTest(t);
        ac.applyTest(t);

        /**  Persistance **/
        //TODO

        /** Report results and send back to model **/













        /** CARGAMOS LOS SOURCES QUE SE VAN A USAR PARA EL ANÁLISIS **/
        /**File filen = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + nameDir);


        //Persistir
        AnalysisEntity analysis = map.getAnaysisEntity(ac);
        entityManager.persist(analysis);

        //Enviar al modelo
        ArrayList<ArrayList<Float>> matrix = new ArrayList<>();
        for(SubmissionEntity sb : analysis.getSubs()){
            for(TestResultEntity rs : sb.getData()){
                matrix.add((ArrayList<Float>) rs.getResult());
            }
        }
        Prueba vista**/
        Double m[][] = new Double[50][50];
        double k = 0.1;
        for(int i=0; i < 50; i++){
            for(int j=0; j < 50; j++){
                m[i][j] = k*j;
            }
        }
        model.addAttribute("resultT", m);
        //**/

        //model.addAttribute("resultT", matrix);
        return "showResultTest";
    }

    @GetMapping
    public String main(){
        return "main";
    }
}
