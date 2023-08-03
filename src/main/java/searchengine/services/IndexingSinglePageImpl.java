package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.IndexingSinglePageResponse;
import searchengine.model.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingSinglePageImpl implements IndexingSinglePageService {

    @Autowired
    IndexRepository indexRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    SessionFactory sessionFactory;

    IndexingSinglePageResponse indexingPageResponse;

    @Override
    public IndexingSinglePageResponse getTrueResult() {
        indexingPageResponse = new IndexingSinglePageResponse(true);
        return indexingPageResponse;
    }

    @Override
    public IndexingSinglePageResponse getFalseResultForIndexPage() {
        String error = "Данная страница находится за пределами сайтов, " +
                "указанных в конфигурационном файле";
        indexingPageResponse = new IndexingSinglePageResponse(false, error);
        return indexingPageResponse;
    }

    @Override
    public IndexingSinglePageResponse getIndexingSinglePageResponse(String url) {

        Iterator<SiteModel> siteModelIterator = siteRepository.findAll().iterator();
        while (siteModelIterator.hasNext()) {
            SiteModel siteModel = siteModelIterator.next();
            String urlSiteInRepo = siteModel.getUrl();
            if (!url.startsWith(urlSiteInRepo)) {
                indexingPageResponse = getFalseResultForIndexPage();
                continue;
            }

            int siteIDFromSiteModel = siteModel.getId();
            String cleanedPathForIndexing = url.replaceAll(urlSiteInRepo, "/");
            updateEntity(cleanedPathForIndexing, siteIDFromSiteModel);
            try {
                rewriteSinglePage(url, cleanedPathForIndexing, siteModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
            indexingPageResponse = getTrueResult();
            break;
        }

        return indexingPageResponse;
    }

    public void updateEntity(String cleanedPathForIndexing, int siteIDFromSiteModel) {
        Optional<Page> pageOptional = pageRepository.findByPathAndSiteId(cleanedPathForIndexing, siteIDFromSiteModel);
        if (!pageOptional.isPresent()) {
            return;
        }
        Page currentPage = pageOptional.get();
        Iterator<IndexModel> indexModelIterator = pageOptional.get().getIndexModels().iterator();

        while (indexModelIterator.hasNext()) {
            IndexModel indexModel = indexModelIterator.next();
            Optional<Lemma> lemmaOptional = lemmaRepository.findByLemmaAndSiteId(indexModel.getLemma().getLemma(), currentPage.getSite().getId());
            if (!lemmaOptional.isPresent()) {
                continue;
            }
            Lemma currentLemma = lemmaOptional.get();
            if (currentLemma.getFrequency() == 1) {
                lemmaRepository.delete(currentLemma);
            } else {
                currentLemma.setFrequency(currentLemma.getFrequency() - 1);
                lemmaRepository.save(currentLemma);
            }
        }

        currentPage.getIndexModels().clear();
        pageRepository.delete(currentPage);

    }

    public void rewriteSinglePage(String pageForIndexing, String cleanedPathForIndexing, SiteModel siteModel) throws IOException {
        SiteParser parser = new SiteParser(pageForIndexing);
        Page page = new Page();
        page.setPath(cleanedPathForIndexing);
        page.setCode(parser.getPageCode(pageForIndexing));
        page.setContent(parser.getContent(pageForIndexing));
        page.setSite(siteModel);
        pageRepository.save(page);

        int pageCode = parser.getPageCode(pageForIndexing);

        FillingDataBaseServiceImpl fillingDataBase = new FillingDataBaseServiceImpl(pageForIndexing, siteRepository, pageRepository, lemmaRepository,
                indexRepository, sessionFactory);

        fillingDataBase.writeToLemmaEntity(pageCode, siteModel, pageForIndexing, page);
    }

}
