package searchengine.services;

import searchengine.dto.statistics.SearchResponse;

public interface SearchService {
    SearchResponse getSearchStatistics(int offset, int limit, String query, String site);
}
