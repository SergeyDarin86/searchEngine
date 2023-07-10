package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter

@Entity
@Table(name = "site")
public class SiteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Page> pageSet;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Lemma> lemmaSet;

    @Column(name = "status", nullable = false, columnDefinition = "ENUM('INDEXING','INDEXED','FAILED')")
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreatedDate
    @Column(name = "status_time", nullable = false)

    private LocalDateTime creationTime = LocalDateTime.now();

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;
}
