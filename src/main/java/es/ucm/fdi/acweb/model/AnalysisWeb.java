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
        public final String source;
        public final String sub1;
        public final String target;
        public final String sub2;
        public final float value;

        public DataPoint(String a, String s1, String b, String s2, float d) {
            this.source = a;
            this.sub1 = s1;
            this.target = b;
            this.sub2 = s2;
            this.value = d;
        }

        @Override
        public String toString() {
            return sub1 + " - " + sub2 + " having distance " + value;
        }
    }

    public static class Node{
        public static Long id;
        public static int group;

        public Node(Long i, int g){
            id = i;
            group = g;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class NodesLinks{
        public List<Node> nodes;
        public final List<DataPoint> links;

        public NodesLinks(List<Node> ids, List<DataPoint> data){
            nodes = ids;
            links = data;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public NodesLinks getGeneralData() {
        ArrayList<DataPoint> r = new ArrayList<>();
        ArrayList<Node> ids = new ArrayList<>();
        for (int i = 0; i < subs.size(); i++) {
            float sum = 0;
            for (int j = 0; j < i; j++) {
                sum +=  subs.get(i).getData().get(0).getResult().get(j);
                r.add(new DataPoint(subs.get(i).getIdAuthors(), subs.get(i).getAnotations(), subs.get(j).getIdAuthors(), subs.get(j).getAnotations(),
                        subs.get(i).getData().get(0).getResult().get(j)));

            }
            ids.add(new Node(subs.get(i).getId(), (int) sum*10));
        }
        return new NodesLinks(ids, r);
    }

    public List<DataPoint> toPoints(){
        return getGeneralData().links;
    }

    public List<DataPoint> toPointsGivenSub(int index){
        ArrayList<DataPoint> r = new ArrayList<>();
        for(int i = 0; i < subs.get(index).getData().get(0).getResult().size(); i++){
            r.add(new DataPoint(subs.get(index).getIdAuthors(), subs.get(index).getAnotations(), subs.get(i).getIdAuthors(), subs.get(i).getAnotations(), subs.get(index).getData().get(0).getResult().get(i)));
        }

        return r;
    }

    public List<DataPoint> suspicious(){
        ArrayList<DataPoint> r = new ArrayList<>();
        for (int i = 0; i < subs.size(); i++) {
            for (int j = 0; j < i; j++) {
                float value = subs.get(i).getData().get(0).getResult().get(j);
                if(value < 0.4){
                    r.add(new DataPoint(subs.get(i).getIdAuthors(), subs.get(i).getAnotations(), subs.get(j).getIdAuthors(), subs.get(j).getAnotations(), value));
                }
            }
        }
        return r;
    }


    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }
}