package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.Submission;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class TestResultWeb {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private SubmissionWeb sub;

    private String testKey; // identificador del test al que hace referencia

    @ElementCollection
    private List<Float> result = new ArrayList<>();

    public TestResultWeb(String key, Object data) {
        this.testKey = key;
        this.result = (List<Float>) data;
    }
}
