package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Submission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data
public class SourceWeb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private SubmissionWeb sub;

   @Lob
    private String code;
    private String fileName;


    public Submission.Source sourceToAc(){
        return new Submission.Source(this.code, this.fileName);
    }

    public static SourceWeb sourceFromAc(Submission.Source source, SubmissionWeb sub){
        SourceWeb sourceWeb = new SourceWeb();

        sourceWeb.setSub(sub);
        sourceWeb.setCode(source.getCode());

        sourceWeb.setFileName(source.getFileName());

        return sourceWeb;
    }

}
