package es.ucm.fdi.acweb.model;

import es.ucm.fdi.ac.Submission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;

@Entity
@NoArgsConstructor
@Data
@NamedQueries({
        @NamedQuery(name="SourceWeb.byFileName",
                query="SELECT s FROM SourceWeb s "
                        + "WHERE s.fileName = : file AND s.sub.id  = : id"),
})
public class SourceWeb implements Transferable<SourceWeb.Transfer>{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
    private Long id;

    @ManyToOne
    private SubmissionWeb sub;

   @Lob
    private String code;
    private String fileName;


    public Submission.Source sourceToAc(){
        return new Submission.Source(this.code, this.fileName);
    }

    public static SourceWeb sourceFromAc(Submission.Source source, SubmissionWeb sub){
        SourceWeb sourceWeb = new SourceWeb();

        sourceWeb.setSub(sub);
        sourceWeb.setCode(source.getCode());

        sourceWeb.setFileName(source.getFileName());

        return sourceWeb;
    }

    @Override
    public int hashCode() {
        return Math.toIntExact(this.id);
    }

    @Getter
    @AllArgsConstructor
    public static class Transfer {
        private String fileName;
        private String code;


    }

    @Override
    public SourceWeb.Transfer toTransfer() {
        return new Transfer(this.fileName, this.code);
    }

    @Override
    public String toString() {
        return toTransfer().toString();
    }


}
