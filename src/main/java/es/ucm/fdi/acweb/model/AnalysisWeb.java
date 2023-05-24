package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.parseLong;


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

    // Class for mapping data of test (necessarily to build d3 components)
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
        public final String id;
        public final int group;

        public Node(String i, int g){
            id = i;
            group = g;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static class NodesLinks{
        public final List<Node> nodes;
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

    /**
     * given index returns number of submissions that have less than 0.4 distance with him
     */
    private int group(int index){
        int sum = 0;
        for(int i= 0; i < subs.size(); i++){
            if(subs.get(index).getData().get(0).getResult().get(i) < 0.4) sum++;
        }
        return sum;
    }

    /**
     * Returns NodesLinks structure associated with test 0
     */
    public NodesLinks getGeneralData() {
        ArrayList<DataPoint> r = new ArrayList<>();
        ArrayList<Node> ids = new ArrayList<>();
        for (int i = 0; i < subs.size(); i++) {
            for (int j = 0; j < i; j++) {
                r.add(new DataPoint(subs.get(i).getIdAuthors(), subs.get(i).getAnotations(), subs.get(j).getIdAuthors(), subs.get(j).getAnotations(),
                        subs.get(i).getData().get(0).getResult().get(j)));

            }
            ids.add(new Node(subs.get(i).getIdAuthors(), group(i)));
        }
        return new NodesLinks(ids, r);
    }

    /**
     * Given index of submission returns list of DataPoints associated with him
     */
    public List<DataPoint> toPointsGivenSub(int index){
        ArrayList<DataPoint> r = new ArrayList<>();
        for(int i = 0; i < subs.get(index).getData().get(0).getResult().size(); i++){
            r.add(new DataPoint(subs.get(index).getIdAuthors(), subs.get(index).getAnotations(), subs.get(i).getIdAuthors(), subs.get(i).getAnotations(), subs.get(index).getData().get(0).getResult().get(i)));
        }

        return r;
    }

    /**
     * Returns DataPoint list containing suspicious (distance less than 0.4) group of subs
     */
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