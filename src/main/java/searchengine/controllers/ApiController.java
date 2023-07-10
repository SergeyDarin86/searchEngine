package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.IndexingSinglePageResponse;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.StartStopResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingSinglePageService;
import searchengine.services.SearchService;
import searchengine.services.StartStopService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Autowired
    SearchService searchService;

    @Autowired
    IndexingSinglePageService singlePageService;

    @Autowired
    StartStopService startService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public synchronized ResponseEntity<StartStopResponse> startIndex() {
        return ResponseEntity.ok(startService.getStartResponse());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<StartStopResponse> stopIndex() {
        return ResponseEntity.ok(startService.getStopResponse());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingSinglePageResponse> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(singlePageService.getIndexingSinglePageResponse(url));
    }


    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam(value = "query") String query
            , @RequestParam(value = "offset", required = false) int offset
            , @RequestParam(value = "limit", required = false) int limit
            , @RequestParam(value = "site", required = false) String site) {

        return ResponseEntity.ok(searchService.getSearchStatistics(offset, limit, query, site));
    }

}
