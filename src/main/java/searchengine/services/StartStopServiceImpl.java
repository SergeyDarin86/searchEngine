package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.StartStopResponse;

@Service
@RequiredArgsConstructor
public class StartStopServiceImpl implements StartStopService {

    StartStopResponse startResponse;

    @Autowired
    FillingDataBaseServiceImpl fillingDataBase;

    @Autowired
    MainStartIndexing service;

    @Override
    public StartStopResponse getStartResponse() {
        if (!fillingDataBase.isStarting()) {
            service.startParsing();
            return getTrueResult();
        } else {
            return getFalseResultForStartIndexing();
        }
    }

    @Override
    public StartStopResponse getStopResponse() {
        if (!fillingDataBase.keepingRunning() || fillingDataBase.doStart()) {
            fillingDataBase.doStop();
            return getTrueResult();
        } else {
            return getFalseResultForStopIndexing();
        }
    }

    @Override
    public StartStopResponse getTrueResult() {
        startResponse = new StartStopResponse(true);
        return startResponse;
    }

    @Override
    public StartStopResponse getFalseResultForStartIndexing() {
        startResponse = new StartStopResponse(false, "Индексация уже запущена");
        return startResponse;
    }

    @Override
    public StartStopResponse getFalseResultForStopIndexing() {
        startResponse = new StartStopResponse(false, "Индексация не запущена");
        return startResponse;
    }
}
