package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainStartIndexing {

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    IndexRepository indexRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    private final SitesList sitesList;

    FillingDataBaseServiceImpl fillingDataBase;

    @Async
    public void startParsing() {
        List<Site> listSitesFromConfig = sitesList.getSites();
        isFileExistInDataBase(listSitesFromConfig);
        for (Site siteFromConfig : listSitesFromConfig) {
            fillingDataBase = new FillingDataBaseServiceImpl(siteFromConfig.getUrl(), siteRepository, pageRepository, lemmaRepository, indexRepository, sessionFactory);
            Thread thread = new Thread(fillingDataBase);
            thread.start();
        }
    }

    public void isFileExistInDataBase(List<Site> siteList) {

        for (Site siteFromConfig : siteList) {
            Iterator<SiteModel> siteModelIterator = siteRepository.findAll().iterator();
            while (siteModelIterator.hasNext()) {
                SiteModel siteModel = siteModelIterator.next();
                String urlSiteInRepo = siteModel.getUrl();
                if (siteFromConfig.getUrl().equals(urlSiteInRepo)) {
                    siteRepository.delete(siteModel);
                    break;
                }
            }
        }

    }

}
