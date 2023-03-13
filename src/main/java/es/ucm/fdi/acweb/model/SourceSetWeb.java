package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class SourceSetWeb {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @OneToOne
    private AnalysisWeb analysis;
    @OneToOne
    private FileTreeNodeWeb sourceRoots;

    public SourceSet castToAc2() throws IOException {
        return new SourceSet(this.sourceRoots.castToAc2());
    }

    public void castToAcWeb(SourceSet sourceSet){
        this.sourceRoots.castToAcWeb(sourceSet.getFilteredTree());
    }

}
