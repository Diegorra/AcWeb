package es.ucm.fdi.AcWeb.model;

import jdk.jfr.Enabled;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "Source")
public class Source_entity {
    @Id
    private Long id;

    @ManyToOne
    private Submission_entity sub_id;

    private String code;
    private String fileName;
}
