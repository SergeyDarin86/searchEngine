package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.statistics.*;
import searchengine.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    LemmaRepository lemmaRepository;

    @Autowired
    IndexRepository indexRepository;

    @Autowired
    SitesList sitesList;

    @Override
    public SearchResponse getSearchStatistics(int offset, int limit, String query, String site) {

        SearchResponse responseNew = new SearchResponse();
        if (query.length() != 0) {
            MainSearch mainSearch = new MainSearch(pageRepository, lemmaRepository, indexRepository, siteRepository, sitesList);
            mainSearch.searchPages(query, site);
            int countOfPages = MainSearch.getCountOfPages();
            responseNew.setCount(countOfPages);
            responseNew.setResult(true);

            List<DetailedSearchItem> detailed = new ArrayList<>();
            try {
                int i = 0;
                for (Map.Entry<Page, Float> entry : MainSearch.getSortedMapByRelevance().entrySet()) {
                    DetailedSearchItem detailedSearchItem = new DetailedSearchItem();
                    Page page = entry.getKey();
                    i++;
                    if (i <= offset || i > (offset + limit)) continue;
                    detailedSearchItem.setSite(page.getSite().getUrl().replaceFirst(".$", ""));
                    detailedSearchItem.setSiteName(page.getSite().getName());
                    detailedSearchItem.setUri(page.getPath());
                    detailedSearchItem.setTitle(mainSearch.getPageTitle(page.getContent()));
                    detailedSearchItem.setSnippet(mainSearch.getSnippet(page, query));
                    detailedSearchItem.setRelevance(entry.getValue());
                    detailed.add(detailedSearchItem);
                }
            } catch (NullPointerException | IOException ignored) {
            }
            responseNew.setData(detailed);
        } else {
            responseNew.setResult(false);
            responseNew.setError("Задан пустой поисковый запрос");
        }
        return responseNew;
    }

}
