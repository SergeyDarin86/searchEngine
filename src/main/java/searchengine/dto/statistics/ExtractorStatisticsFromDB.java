package searchengine.dto.statistics;

import searchengine.model.SiteModel;
import searchengine.model.SiteRepository;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ExtractorStatisticsFromDB {

    SiteRepository siteRepository;

    public ExtractorStatisticsFromDB(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public String getStatusOfIndexing(String urlFromApplication) {
        Optional<SiteModel> statusByName = siteRepository.siteByNameOptional(urlFromApplication);
        String status = "";
        if (statusByName.isPresent()) {
            status = statusByName.get().getStatus().toString();
        }
        return status;
    }

    public String getLastError(String urlFromApplication) {
        Optional<SiteModel> statusByName = siteRepository.siteByNameOptional(urlFromApplication);
        String lastError = "";
        if (statusByName.isPresent()) {
            lastError = statusByName.get().getLastError();
        }
        return lastError;
    }

    public long getStatusTime(String urlFromApplication) throws SQLException, ParseException {
        Optional<SiteModel> statusByName = siteRepository.siteByNameOptional(urlFromApplication);
        String statusTime2;
        Date dateToLong = new Date();
        if (statusByName.isPresent()) {
            statusTime2 = statusByName.get().getCreationTime().toString();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            dateToLong = format.parse(statusTime2.replace('T', ' '));
        }
        return dateToLong.getTime();
    }

    public int getCountOfPagesForSite(String urlFromApplication) throws SQLException {
        return siteRepository.countOfPagesForSite(urlFromApplication);
    }

    public int getCountOfLemmasForSite(String urlFromApplication) throws SQLException {
        return siteRepository.countOfLemmasForSite(urlFromApplication);
    }

}
