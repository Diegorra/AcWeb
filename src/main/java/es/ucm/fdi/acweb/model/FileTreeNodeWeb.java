package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.extract.FileTreeNode;
import es.ucm.fdi.acweb.LocalData;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class FileTreeNodeWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    private String path; // relative to analysis path

    @OneToOne
    private SourceSetWeb ss;

    @ManyToOne
    private FileTreeNodeWeb parent;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="parent_id")
    private List<FileTreeNodeWeb> children = new ArrayList<>();

    public FileTreeNode castToAc2(){
        return new FileTreeNode(new File(this.path), this.parent.castToAc2());
    }

    public void castToAcWeb(FileTreeNode fileTreeNode){
        this.path = fileTreeNode.getPath();
        //this.parent = fileTreeNode.getParent(); OJO ----> TIPO TreeNode

        //Persistimos los childen
        this.children.clear();
        for(FileTreeNode i : fileTreeNode.getChildren()){
            FileTreeNodeWeb n = new FileTreeNodeWeb();
            n.castToAcWeb(i);
            this.children.add(n);
        }
    }

}
