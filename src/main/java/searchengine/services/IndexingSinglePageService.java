package searchengine.services;

import searchengine.dto.statistics.IndexingSinglePageResponse;

public interface IndexingSinglePageService {
    IndexingSinglePageResponse getIndexingSinglePageResponse(String url);
    IndexingSinglePageResponse getTrueResult();
    IndexingSinglePageResponse getFalseResultForIndexPage();
}
