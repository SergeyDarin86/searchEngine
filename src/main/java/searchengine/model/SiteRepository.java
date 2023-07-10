package searchengine.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository <SiteModel,Integer> {

    @Transactional
    @Query(value = "SELECT * FROM site where site.url = ?1", nativeQuery = true)
    SiteModel siteByName(String site);

    @Transactional
    @Query(value = "select * from site where url = ?1", nativeQuery = true)
    Optional <SiteModel> siteByNameOptional(String site);

    @Transactional
    @Query(value = "select count(*) as recordCount from search_engine.site s " +
            "join search_engine.lemma l on l.site_id = s.id where s.url = ?1", nativeQuery = true)
    int countOfLemmasForSite(String site);

    @Transactional
    @Query(value = "select count(*) as recordCount from search_engine.site s " +
            "join search_engine.page p on p.site_id = s.id where s.url = ?1", nativeQuery = true)
    int countOfPagesForSite(String site);
}
