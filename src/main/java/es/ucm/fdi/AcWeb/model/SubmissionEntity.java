package es.ucm.fdi.AcWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Table(name = "Submission")
public class SubmissionEntity {
    @Id
    private Long id;

    @ManyToOne
    private AnalysisEntity analysis;

    private String originalPath;
    private Integer internalId;

    private String hash;

    private boolean hashUpToDate;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="sub_id")
    private List<SourceEntity> sourceRoots = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "sub_id")
    private List<TestResultEntity> data = new ArrayList<>();



}
