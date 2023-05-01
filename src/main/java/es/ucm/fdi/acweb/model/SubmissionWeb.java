package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Submission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@NoArgsConstructor
@Data
@NamedQueries({
        @NamedQuery(name="SubmissionWeb.byInternalId",
                query="SELECT s FROM SubmissionWeb s "
                        + "WHERE s.internalId = : id AND s.analysis.id  = : analysisId"),
        @NamedQuery(name="SubmissionWeb.byIdAuthors",
                query="SELECT s FROM SubmissionWeb s "
                        + "WHERE s.idAuthors = : id AND s.analysis.id  = : analysisId"),
})
public class SubmissionWeb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private AnalysisWeb analysis;

    private String originalPath;
    private String idAuthors;
    private Integer internalId;

    private String hash;
    private String anotations;

    private boolean hashUpToDate;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="sub_id")
    private List<SourceWeb> sourceRoots = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "sub_id")
    private List<TestResultWeb> data = new ArrayList<>();

    /*public Submission submissionToAc(){
        Submission sub = new Submission(this.id_authors, this.originalPath, this.internalId);
        //hasUpToDate

        for(SourceWeb s : this.getSourceRoots()){
            sub.addSource(new File(s.getFileName())); //coge file en base al path que almacena el FileName?
        }

        for(TestResultWeb i : this.getData()){
            sub.putData(i.getTestKey(), i.getResult());
        }
        return sub;
    }

    public static SubmissionWeb submissionFromAc(Submission submission, AnalysisWeb analysis){
        SubmissionWeb submissionWeb = new SubmissionWeb();

        submissionWeb.setAnalysis(analysis);
        submissionWeb.setOriginalPath(submission.getOriginalPath());
        submissionWeb.setId_authors(submission.getId());
        submissionWeb.setInternalId(submission.getInternalId());
        submissionWeb.setHash(submission.getHash());
        //this.hashUpToDate = ???

        //Persistimos los sourceRoots
        for(Submission.Source i : submission.getSources()){
            submissionWeb.getSourceRoots().add(sourceFromAc(i, submissionWeb));
        }


        return submissionWeb;
    }*/

    public void fromAc(Submission submission, AnalysisWeb analysis, List<SourceWeb> sourceRoots){
        this.setAnalysis(analysis);
        this.setOriginalPath(submission.getOriginalPath());
        this.setIdAuthors(submission.getId());
        this.setInternalId(submission.getInternalId());
        this.setHash(submission.getHash());
        this.setSourceRoots(sourceRoots);
    }

    /*
    public void persistData(String key, Submission sub){
        this.data.add(testResultFromAc(key, sub.getData(key), this));
    }*/

    public void setNames(Map<String, String> naming){
        if(naming.containsKey(idAuthors)){
            this.setAnotations(naming.get(idAuthors));
        }
    }

    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }
}
