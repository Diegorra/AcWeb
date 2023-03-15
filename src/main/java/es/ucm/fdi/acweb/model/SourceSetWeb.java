package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static es.ucm.fdi.acweb.model.FileTreeNodeWeb.ftnFromAc;

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

    public SourceSet sourceSetToAc() throws IOException {
        return new SourceSet(this.sourceRoots.ftnToAc());
    }

    /*public static SourceSetWeb sourceSetFromAc(SourceSet sourceSet, AnalysisWeb analysis){
        SourceSetWeb sourceSetWeb = new SourceSetWeb();

        sourceSetWeb.setAnalysis(analysis);
        sourceSetWeb.setSourceRoots(ftnFromAc(sourceSet.getFilteredTree(), sourceSetWeb, null));

        return sourceSetWeb;
    }*/

    public void fromAc(FileTreeNodeWeb ftnw, AnalysisWeb analysis){

        this.setAnalysis(analysis);
        this.setSourceRoots(ftnw);

    }
}
