package es.ucm.fdi.AcWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Table(name = "FileTreeNode")
public class FileTreeNodeEntity {
    @Id
    private Long id;

    @ManyToOne
    private SourceSetEntity sources;

    private File f;

    private File original;

    @ManyToOne
    private FileTreeNodeEntity parent;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="parent")
    private List<FileTreeNodeEntity> children = new ArrayList<>();
}
