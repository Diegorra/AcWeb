package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
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

    // lista de puntos, cada uno con sub1, sub2, y distancia
    public static class DataPoint {
        public final String sub1;
        public final String sub2;
        public final float value;

        public DataPoint(String a, String b, float d) {
            this.sub1 = a;
            this.sub2 = b;
            this.value = d;
        }
    }

    public List<DataPoint> toPoints() {
        ArrayList<DataPoint> r = new ArrayList<>();
        for (int i = 0; i < subs.size(); i++) {
            for (int j = 0; j < i; j++) {
                r.add(new DataPoint(subs.get(i).getIdAuthors(), subs.get(j).getIdAuthors(),
                        subs.get(i).getData().get(0).getResult().get(j)));
            }
        }
        return r;
    }

    public List<DataPoint> toPointsGivenSub(int index){
        ArrayList<DataPoint> r = new ArrayList<>();
        for(int i = 0; i < subs.get(index).getData().get(0).getResult().size(); i++){
            r.add(new DataPoint(subs.get(index).getIdAuthors(), subs.get(i).getIdAuthors(), subs.get(index).getData().get(0).getResult().get(i)));
        }

        return r;
    }


    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }
}