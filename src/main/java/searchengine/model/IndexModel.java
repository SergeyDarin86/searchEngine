package searchengine.model;

import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter

@Entity

@Table(name = "`index`")
@org.hibernate.annotations.Immutable
public class IndexModel {

    public IndexModel() {
    }

    @Embeddable
    public static class ComplexID implements Serializable {
        @Column(name = "page_id", nullable = false)
        protected Integer pageId;
        @Column(name = "lemma_id", nullable = false)
        protected Integer lemmaId;

        public ComplexID() {
        }

        public ComplexID(int pageId, int lemmaId) {
            this.pageId = pageId;
            this.lemmaId = lemmaId;
        }

        public boolean equals(Object o) {
            if (o instanceof ComplexID that) {
                return this.pageId.equals(that.pageId)
                        && this.lemmaId.equals(that.lemmaId);
            }
            return false;
        }

        public int hashCode() {
            return pageId.hashCode() + lemmaId.hashCode();
        }

    }

    @EmbeddedId
    protected ComplexID complexID = new ComplexID();

    @Column(name = "`rank`", updatable = false)
    @NotNull
    protected Float rank;

    @ManyToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH})
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(
            name = "page_id",
            insertable = false, updatable = false)
    protected Page page;

    @ManyToOne(cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.REFRESH})
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(
            name = "lemma_id",
            insertable = false, updatable = false)
    protected Lemma lemma;
}
