package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.Submission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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


    public Analysis castToAc2() throws IOException {
        Analysis ac = new Analysis();
        ac.loadSources(this.sourceSet.castToAc2()); //esto ya inicializa las submission
        return ac;
    }

    public void castToAcWeb(Analysis ac){
        //this.sourceSet.castToAcWeb(ac); -> No puedo obtener el sourceSet de un ac

        subs.clear(); // eliminamos antiguos para no duplicar valores
        for(Submission sub : ac.getSubmissions()){
            SubmissionWeb submissionWeb = new SubmissionWeb();
            submissionWeb.castToAcWeb(sub);

            for(String testKey : appliedTestKey){//para cada submission chequeamos si se ha aplicado alguno de los test, en cuyo caso lo persistimos
                if(ac.hasResultsForKey(testKey)){
                    submissionWeb.persistData(testKey, sub);
                }
            }
        }

    }
}