package es.ucm.fdi.AcWeb.model;

import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Table(name = "Test")
public class Test_entity {
    @Id
    private Long id;

    @OneToOne
    private Analysis_entity analysis_id;

    private String testKey;

    //private String[] requires = new String[0];

    //private String[] provides = new String[0];

    private Boolean independentPreprocessing;
    private Boolean independentSimilarity;

    private Boolean testCanceled;
    private Float progress;
}
