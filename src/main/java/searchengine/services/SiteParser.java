package searchengine.services;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;


public class SiteParser extends RecursiveTask<Set<String>> {

    private final String urlSite;

    @Getter
    @Setter
    private static HashSet<String> finalSet = new HashSet<>();

    public SiteParser(String urlSite) {
        this.urlSite = urlSite;
    }

    @Override
    protected Set<String> compute() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            Document document = getDocument(urlSite);
            List<SiteParser> tasks = new ArrayList<>();
            if (!(document == null)) {
                Elements elements = document.select("body").select("a");
                for (Element element : elements) {
                    String middleURL = element.absUrl("href");
                    boolean isContain = getFinalSet().contains(middleURL);
                    if (isContain) {
                        continue;
                    }
                    if (middleURL.startsWith(urlSite)
                            && !middleURL.contains("#")
                            && !middleURL.contains("pdf")
                            && !middleURL.contains("jpeg")
                            && !(middleURL.contains("jpg") || middleURL.contains("JPG"))
                            && !(middleURL.contains("png"))) {
                        finalSet.add(middleURL);
                        SiteParser task = new SiteParser(middleURL);
                        task.fork();
                        tasks.add(task);
                    }
                }
                addResultsFromTasks(finalSet, tasks);
            }
        } catch (Exception ignored) {
        }
        return getFinalSet();
    }

    private void addResultsFromTasks(HashSet<String> set, List<SiteParser> tasks) {
        for (SiteParser item : tasks) {
            set.addAll(item.join());
        }
        setFinalSet(set);
    }

    public int getPageCode(String middleUrl) throws IOException {
        Connection.Response response;
        response = Jsoup.connect(middleUrl)
                .followRedirects(false)
                .ignoreHttpErrors(true)
                .execute();

        return response.statusCode();
    }

    public Document getDocument(String middleUrl) throws IOException {
        Document document;
        document = Jsoup.connect(middleUrl)
                .timeout(30000)
                .ignoreHttpErrors(true)
                .followRedirects(false)
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1;" +
                        "en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get();
        return document;
    }

    public String getContent(String middleUrl) throws IOException {
        String content = "";
        if (!(getDocument(middleUrl) == null)) {
            content = getDocument(middleUrl).html();
        }
        return content;
    }

}
