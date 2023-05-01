package es.ucm.fdi.acweb;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.web.multipart.MultipartFile;

public class ZipFileExtractor {

    public void extractZip(MultipartFile zipFile, Path outputDirectory, Map<String, String> naming) throws IOException {
        File tempFile = File.createTempFile("zip", null);
        zipFile.transferTo(tempFile);
        extractZip(tempFile, outputDirectory, naming);
        tempFile.delete();
    }

    private void extractZip(File zipFile, Path outputDirectory, Map<String, String> naming) throws IOException {
        // Crea un patrón de expresión regular para capturar un código numérico de 7 dígitos
        Pattern pattern = Pattern.compile("^([\\p{L} ]+)_(\\d{7})_.*");
        try (ZipFile zf = new ZipFile(zipFile, Charset.forName("CP437"))) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                Path filePath = outputDirectory.resolve(zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zf.getInputStream(zipEntry), filePath);

                    if (isZipFile(filePath)) {
                        // Extrae el código numérico de 7 dígitos del nombre del archivo y el nombre del autor
                        Matcher matcher = pattern.matcher(zipEntry.getName());
                        String subfolderName;
                        if (matcher.find()) {
                            String name = matcher.group(1);
                            subfolderName = matcher.group(2);
                            naming.put(subfolderName, name);
                        } else { //si no encuentra el patrón
                            subfolderName = zipEntry.getName().replaceAll("\\.zip$", "");
                            naming.put(subfolderName, subfolderName);
                        }

                        // Crea una carpeta con el nombre subfolderName
                        Path subfolder = outputDirectory.resolve(subfolderName);
                        Files.createDirectories(subfolder);
                        // Extrae el contenido del archivo ZIP en la carpeta creada
                        extractZip(filePath.toFile(), subfolder, naming);
                        // Elimina el archivo ZIP
                        Files.delete(filePath);
                    }
                }
            }
        }
    }

    private boolean isZipFile(Path file) {
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            byte[] signature = new byte[4];
            if (fis.read(signature) != 4) {
                return false;
            }
            int sig = ((signature[0] & 0xFF) << 24) | ((signature[1] & 0xFF) << 16) | ((signature[2] & 0xFF) << 8) | (signature[3] & 0xFF);
            return sig == 0x504B0304;
        } catch (IOException e) {
            return false;
        }
    }

    public void clean(File[] files, ArrayList<String> filters) throws IOException {
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getAbsolutePath());
                clean(file.listFiles(), filters);
            } else {
                String fileExtension = getFileExtension(file.getName());
                if(!filters.contains(fileExtension) && !filters.contains(file.getName())){
                    Files.delete(file.toPath());
                }
            }
        }
    }

    private static String getFileExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return ""; // No se encontró un punto en el nombre del archivo
        }
        return filename.substring(index + 1);
    }

}
