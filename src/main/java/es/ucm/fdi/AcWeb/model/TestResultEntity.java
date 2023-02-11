package es.ucm.fdi.AcWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Table(name = "TestResultEntity")
public class TestResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private SubmissionEntity sub;

    private String key; // identificador del test al que hace referencia

    @ElementCollection
    private List<Float> result = new ArrayList<>();


}
