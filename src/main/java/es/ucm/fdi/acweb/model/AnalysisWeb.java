package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.Submission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Entity
@NoArgsConstructor
@Data
public class AnalysisWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private User owner;

    private String name;

    @OneToOne
    @JoinColumn(name="analysis_id")
    private SourceSetWeb sourceSet;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="analysis_id")
    private List<SubmissionWeb> subs = new ArrayList<>();

    @ElementCollection
    private List<String> appliedTestKey;


    public Analysis analysisToAc(File basePath) throws IOException {
        Analysis ac = new Analysis();
        ac.loadSources(this.sourceSet.sourceSetToAc(basePath)); //esto ya inicializa las submissions
        return ac;
    }

    /*public void analysisFromAc(Analysis ac, User owner, SourceSetWeb sourceSetWeb, String name){

        this.setOwner(owner);
        this.setName(name);
        this.setSourceSet(sourceSetWeb);

        this.getSubs().clear();
        for(Submission sub : ac.getSubmissions()){
            SubmissionWeb subWeb = submissionFromAc(sub, this);
            this.getSubs().add(subWeb);
        }

    }*/

    public void fromAc(User owner, SourceSetWeb sourceSetWeb, ArrayList<SubmissionWeb> subs, String name){

        this.setOwner(owner);
        this.setName(name);
        this.setSourceSet(sourceSetWeb);
        this.setSubs(subs);

    }

    /*public void persistData(Analysis ac, ArrayList<String> keys){
        for(Submission sub : ac.getSubmissions()){
            SubmissionWeb subWeb = this.getSubs().get(sub.getInternalId());
            for(String testKey : keys){//para cada submission chequeamos si se ha aplicado alguno de los test, en cuyo caso lo persistimos
                if(ac.hasResultsForKey(testKey)){
                    subWeb.persistData(testKey, sub);
                }
            }
        }
    }*/

    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }
}