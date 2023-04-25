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

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name="analysis_id")
    private SourceSetWeb sourceSet;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="analysis_id")
    private List<SubmissionWeb> subs = new ArrayList<>();

    @ElementCollection
    private List<String> appliedTestKey = new ArrayList<>();

    @ElementCollection
    private List<String> filters = new ArrayList<>();


    public Analysis analysisToAc(File basePath) throws IOException {
        Analysis ac = new Analysis();
        ac.loadSources(this.sourceSet.sourceSetToAc(basePath)); //esto ya inicializa las submissions
        return ac;
    }


    public void fromAc(User owner, SourceSetWeb sourceSetWeb, ArrayList<SubmissionWeb> subs, String name){

        this.setOwner(owner);
        this.setName(name);
        this.setSourceSet(sourceSetWeb);
        this.setSubs(subs);

    }

    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }
}