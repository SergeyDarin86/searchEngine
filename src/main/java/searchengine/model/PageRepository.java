package searchengine.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    Optional<Page> findByPathAndSiteId(String path, int siteId);

    @Transactional
    @Query(value = """
            SELECT * FROM search_engine.page p
            JOIN search_engine.index i on i.page_id = p.id
            JOIN search_engine.lemma l on l.id = i.lemma_id
            where l.id = ?1""", nativeQuery = true)
    List<Page> pageListByLemmaID(int lemmaID);

    @Transactional
    @Query(value = """
            SELECT * FROM search_engine.page p
            JOIN search_engine.index i on i.page_id = p.id
            JOIN search_engine.lemma l on l.id = i.lemma_id
            where l.lemma = ?1""", nativeQuery = true)
    List<Page> pageListByLemmaName(String lemma);

}
