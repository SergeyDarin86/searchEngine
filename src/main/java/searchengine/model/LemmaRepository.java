package searchengine.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    Optional<Lemma> findByLemmaAndSiteId(String lemma, int siteID);

    @Transactional
    @Query(value = "SELECT sum(frequency) FROM lemma where lemma.lemma = ?1 GROUP BY lemma.lemma", nativeQuery = true)
    int commonFrequencyForAllSites(String lemma);

    @Transactional
    @Query(value = "SELECT * FROM lemma WHERE lemma.lemma = ?1", nativeQuery = true)
    List<Lemma> lemmaFromDB(String lemma);

    @Transactional
    @Query(value = "SELECT * FROM lemma WHERE lemma.lemma = ?1 && lemma.site_id = ?2", nativeQuery = true)
    List<Lemma> lemmaFromDBbyNameAndSite(String lemma, int siteId);

}
