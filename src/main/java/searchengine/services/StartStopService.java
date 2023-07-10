package searchengine.services;

import searchengine.dto.statistics.StartStopResponse;

public interface StartStopService {
    StartStopResponse getStartResponse();
    StartStopResponse getStopResponse();
    StartStopResponse getTrueResult();
    StartStopResponse getFalseResultForStartIndexing();
    StartStopResponse getFalseResultForStopIndexing();
}
