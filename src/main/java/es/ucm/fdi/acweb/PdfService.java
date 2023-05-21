package es.ucm.fdi.acweb;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import es.ucm.fdi.acweb.controller.AnalysisController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;
    private static final Logger log = LogManager.getLogger(AnalysisController.class);

    @Autowired
    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void createPdf(String html, String out) {

        try (OutputStream os = new FileOutputStream(out);) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useSVGDrawer(new BatikSVGDrawer());
            builder.useFastMode();
            builder.withHtmlContent(html, "");
            builder.toStream(os);
            builder.run();

        } catch (Exception e) {
            log.error("Exception while generating pdf : {}", e);
        }
        //return new File(out);
    }

    public String createHtmlFromTemplate(String templateName, Context context) {
        return templateEngine.process(templateName, context);
    }
}
