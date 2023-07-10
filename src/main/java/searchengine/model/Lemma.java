package searchengine.model;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Getter
@Setter

@Entity

@Table(name = "lemma", schema = "search_engine")
public class Lemma implements Comparable<Lemma>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SiteModel site;

    @Column(columnDefinition = "VARCHAR(255), UNIQUE KEY lemmaIndex (lemma,site_id)")
    private String lemma;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @Override
    public int compareTo(Lemma l) {
        return Integer.compare(this.frequency, l.frequency);
    }
}
