package es.ucm.fdi.AcWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Table(name="Analysis")
public class AnalysisEntity {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name="analysis_id")
    private SourceSetEntity sourceSet;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="analysis_id")
    private List<SubmissionEntity> subs = new ArrayList<>();


    /*
    @OneToMany
    @JoinColumn(name="analysis_id")
    private List<TestEntity> appliedTest = new ArrayList<>();
    */
}