package es.ucm.fdi.AcWeb.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name="SourceSet")
public class SourceSet_entity {
    @Id
    private Long id;

    @OneToOne
    private Analysis_entity analysis_id;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="sourceSet_id")
    private List<FileTreeNode_entity> sourceRoots = new ArrayList<>();
}
