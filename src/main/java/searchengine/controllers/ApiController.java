package searchengine.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api("Search Engine API")
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
    @ApiOperation(value = "Start/Stop indexing",notes = "We can stop indexing process")
    public ResponseEntity<StartStopResponse> stopIndex() {
        return ResponseEntity.ok(startService.getStopResponse());
    }

    @PostMapping("/indexPage")
    @ApiOperation(value = "add/update single Page to index by URL")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successfully post if the path of page included in some site from configuration file"),
            @ApiResponse(code = 200, message = "unsuccessfully operation because of page path")
    })
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
