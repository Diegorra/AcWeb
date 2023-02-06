package es.ucm.fdi.AcWeb.model;

import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "Submission")
public class Submission_entity {
    @Id
    private Long id;

    @OneToOne
    private Analysis_entity analysis_id;

    private String originalPath;
    private Integer internalId;

    private String hash;

    private Boolean hashUpToDate;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name="sub_id")
    private List<Source_entity> sourceRoots = new ArrayList<>();

    //private HashMap<String, Object> data = new HashMap<>();



}
