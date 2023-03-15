package es.ucm.fdi.acweb.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static TestResultWeb testResultFromAc(String key, Object data, SubmissionWeb sub) {
        TestResultWeb testResultWeb = new TestResultWeb();
        testResultWeb.setSub(sub);
        testResultWeb.setTestKey(key);
        testResultWeb.setResult(Arrays.asList(ArrayUtils.toObject((float[])data)));

        return testResultWeb;
    }
}
