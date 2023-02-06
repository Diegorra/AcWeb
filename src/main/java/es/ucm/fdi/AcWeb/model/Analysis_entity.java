package es.ucm.fdi.AcWeb.model;

import es.ucm.fdi.ac.Analysis;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name="Analysis")
public class Analysis_entity {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name="analysis_id")
    private SourceSet_entity sourceSet;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="analysis_id")
    private List<Submission_entity> subs = new ArrayList<>();

    @OneToMany
    @JoinColumn(name="analysis_id")
    private HashSet<Test_entity> appliedTest = new HashSet<Test_entity>();

}