package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.*;
import searchengine.model.SiteRepository;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;

    @Autowired
    SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());

            ExtractorStatisticsFromDB extractorStatistics = new ExtractorStatisticsFromDB(siteRepository);

            try {
                int pages = extractorStatistics.getCountOfPagesForSite(site.getUrl());
                int lemmas = extractorStatistics.getCountOfLemmasForSite(site.getUrl());

                item.setPages(pages);
                item.setLemmas(lemmas);
                item.setStatus(extractorStatistics.getStatusOfIndexing(site.getUrl()));
                item.setError(extractorStatistics.getLastError(site.getUrl()));
                item.setError(extractorStatistics.getLastError(site.getUrl()));
                item.setStatusTime(extractorStatistics.getStatusTime(site.getUrl()));
                total.setPages(total.getPages() + pages);
                total.setLemmas(total.getLemmas() + lemmas);
                detailed.add(item);

            } catch (SQLException | ParseException ignored) {
            }
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
