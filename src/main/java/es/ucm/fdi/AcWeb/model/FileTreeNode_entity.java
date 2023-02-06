package es.ucm.fdi.AcWeb.model;

import jdk.jfr.Enabled;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "FileTreeNode")
public class FileTreeNode_entity {
    @Id
    private Long id;

    @ManyToOne
    private SourceSet_entity sourceSet_id;

    private File f;

    private File original;

    @OneToOne
    private FileTreeNode_entity parent;

    @OneToMany
    @JoinColumn(name="parent")
    private List<FileTreeNode_entity> children = new ArrayList<>();
}
