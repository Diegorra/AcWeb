package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.Submission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class SubmissionWeb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private AnalysisWeb analysis;

    private String originalPath;
    private Integer internalId;

    private String hash;

    private boolean hashUpToDate;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="sub_id")
    private List<SourceWeb> sourceRoots = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "sub_id")
    private List<TestResultWeb> data = new ArrayList<>();

    public Submission castToAc2(){
        Submission sub = new Submission(this.hash, this.originalPath, this.internalId);
        //hasUpToDate

        for(SourceWeb s : sourceRoots){
            sub.addSource(new File(s.getFileName())); //coge file en base al path que almacena el FileName?
        }

        for(TestResultWeb i : data){
            sub.putData(i.getTestKey(), i.getResult());
        }
        return sub;
    }

    public void castToAcWeb(Submission submission){
        this.originalPath = submission.getOriginalPath();
        this.internalId = submission.getInternalId();
        this.hash = submission.getHash();
        //this.hashUpToDate = ???

        //Persistimos los sourceRoots
        this.sourceRoots.clear();
        for(Submission.Source i : submission.getSources()){
            SourceWeb s = new SourceWeb();
            s.castToAcWeb(i);
            this.sourceRoots.add(s);
        }

    }

    public void persistData(String key, Submission sub){
        this.data.add(new TestResultWeb(key, sub.getData(key)));
    }

}
