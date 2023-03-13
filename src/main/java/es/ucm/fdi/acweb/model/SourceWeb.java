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

    private String code;
    private String fileName;


    public Submission.Source castToAc2(){
        return new Submission.Source(this.code, this.fileName);
    }

    public void castToAcWeb(Submission.Source source){
        this.code = source.getCode();
        this.fileName = source.getFileName();
    }

}
