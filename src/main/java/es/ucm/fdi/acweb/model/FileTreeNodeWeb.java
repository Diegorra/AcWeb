package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.extract.FileTreeModel;
import es.ucm.fdi.ac.extract.FileTreeNode;
import es.ucm.fdi.acweb.LocalData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Entity
@NoArgsConstructor
@Data
public class FileTreeNodeWeb implements Transferable<FileTreeNodeWeb.Transfer>{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    private String path; // relative to analysis path

    @OneToOne
    private SourceSetWeb ss;

    @ManyToOne
    private FileTreeNodeWeb parent;

    @OneToMany(cascade=CascadeType.ALL)
    @JoinColumn(name="parent_id")
    private List<FileTreeNodeWeb> children = new ArrayList<>();

    public FileTreeNode ftnToAc(File basePath){
        while (basePath.listFiles().length == 1) {
            basePath = basePath.listFiles()[0];
        }

        // Load Sources
        FileTreeModel ftm = new FileTreeModel();
        for (File root : basePath.listFiles()) {
            ftm.addSource(root);
        }

        return (FileTreeNode) ftm.getRoot();
    }

    public static FileTreeNodeWeb ftnFromAc(FileTreeNode fileTreeNode, SourceSetWeb sourceSetWeb, FileTreeNodeWeb parent){
        FileTreeNodeWeb fileTreeNodeWeb = new FileTreeNodeWeb();

        //Persistimos los childen
        for(FileTreeNode i : fileTreeNode.getChildren()){
           fileTreeNodeWeb.getChildren().add(ftnFromAc(i, sourceSetWeb, fileTreeNodeWeb));
        }
        //Persistimos otros valores
        fileTreeNodeWeb.setPath(fileTreeNode.getPath());
        fileTreeNodeWeb.setSs(sourceSetWeb);
        fileTreeNodeWeb.setParent(parent);

        return fileTreeNodeWeb;
    }

    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }

    @Getter
    @AllArgsConstructor
    public static class Transfer {
        private String text;
        private String href;

        private boolean selectable;
        private ArrayList<FileTreeNodeWeb.Transfer> nodes;
    }

    @Override
    public FileTreeNodeWeb.Transfer toTransfer() {
        ArrayList<FileTreeNodeWeb.Transfer> childrenNodes = new ArrayList<>();
        for(FileTreeNodeWeb i : this.children){
            childrenNodes.add(new FileTreeNodeWeb.Transfer(i.path, i.path, false, i.toTransfer().nodes));
        }
        return new FileTreeNodeWeb.Transfer(path, path, false, childrenNodes);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }

}
