package es.ucm.fdi.AcWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Table(name="Analysis")
public class AnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @OneToOne
    @JoinColumn(name="analysis_id")
    private SourceSetEntity sourceSet;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="analysis_id")
    private List<SubmissionEntity> subs = new ArrayList<>();


    @ElementCollection
    private List<String> appliedTestKey;
}