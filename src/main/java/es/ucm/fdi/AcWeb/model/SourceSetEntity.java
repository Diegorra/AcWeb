package es.ucm.fdi.AcWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Table(name="SourceSet")
public class SourceSetEntity {
    @Id
    private Long id;

    @OneToOne
    private AnalysisEntity analysis;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="sourceSet_id")
    private List<FileTreeNodeEntity> sourceRoots = new ArrayList<>();
}
