package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import searchengine.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class FillingDataBaseServiceImpl implements Runnable {

    private static HashSet<String> set = new HashSet<>();

    IndexRepository indexRepository;

    LemmaRepository lemmaRepository;

    SiteRepository siteRepository;

    PageRepository pageRepository;

    SessionFactory sessionFactory;

    private static ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    private String url;

    public FillingDataBaseServiceImpl(String url, SiteRepository siteRepository, PageRepository pageRepository,
                                      LemmaRepository lemmaRepository, IndexRepository indexRepository, SessionFactory sessionFactory) {
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.sessionFactory = sessionFactory;
    }

    private volatile boolean doStop = false;

    public synchronized Boolean doStart() {
        this.doStop = false;
        return true;
    }

    public boolean keepingRunning() {
        return !this.doStop;
    }

    public synchronized boolean isStarting() {
        return !doStop() && set.size() != 0;
    }

    public synchronized Boolean doStop() {
        pool.shutdownNow();
        Thread.currentThread().interrupt();
        pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        set.clear();
        this.doStop = true;
        return true;
    }

    @Override
    public void run() {

        if (keepingRunning() && !Thread.currentThread().isInterrupted()) {
            SiteModel siteModel = new SiteModel();
            siteModel.setName(getNameOfSite(url));
            siteModel.setStatus(Status.INDEXING);
            siteModel.setUrl(url);
            siteModel.setLastError("");
            siteModel.setCreationTime(LocalDateTime.now());
            siteRepository.save(siteModel);
            SiteParser parser = new SiteParser(url);
            set = SiteParser.getFinalSet();
            pool.invoke(parser);
            writeToPageEntity(set, siteModel, parser, url);
            siteModel.setCreationTime(LocalDateTime.now());
            siteModel.setStatus(Status.INDEXED);
            writeLastError(set, siteModel, url);
            siteRepository.save(siteModel);
        }

    }

    public String getNameOfSite(String url) {
        String regex = "[h]?[t]?[t]?[p]?[s]?[:]?[/]+[w]{0,3}[.]?";
        String cleanedName = url.replaceAll(regex, "");
        StringBuilder nameOfSite = new StringBuilder();
        nameOfSite.append(cleanedName.substring(0, 1).toUpperCase(Locale.ROOT));
        for (int i = 1; i < cleanedName.length(); i++) {
            nameOfSite.append(cleanedName.charAt(i));
        }
        return nameOfSite.toString();
    }

    public synchronized void writeToPageEntity(HashSet<String> set, SiteModel siteModel, SiteParser parser, String parentURL) {

        Spliterator<String> siteModelIterator = set.spliterator();
        ArrayList<Lemma> lemmaArrayList = new ArrayList<>();

        try {
            siteModelIterator.forEachRemaining(url -> {
                if (url.startsWith(parentURL)) {
                    Page page = new Page();
                    try {
                        int pageCode = parser.getPageCode(url);
                        page.setCode(pageCode);
                        page.setContent(parser.getContent(url));
                        page.setSite(siteModel);
                        String relativeLink = url.replaceAll(parentURL, "/");
                        page.setPath(relativeLink);
                        pageRepository.save(page);
                        writeToLemmaEntity(pageCode, siteModel, url, page, lemmaArrayList);
                    } catch (Exception ignored) {
                    }
                }
            });

        } catch (Exception ignored) {
        }

    }

    public void writeToLemmaEntity(int pageCode, SiteModel siteModel, String url, Page page,
                                   ArrayList<Lemma> lemmaArrayList) throws IOException {

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        if (pageCode < 400 && pageCode != 302 && pageCode != 301) {
            GetLemma getLemma = new GetLemma();

            Map<String, Integer> map = getLemma.getMapOfLemmasFromHTML(url);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                Optional<Lemma> lemmasOptional = lemmaRepository.findByLemmaAndSiteId(entry.getKey(), siteModel.getId());
                Lemma newLemmaForDB = new Lemma();
                if (lemmasOptional.isEmpty()) {
                    newLemmaForDB.setLemma(entry.getKey());
                    newLemmaForDB.setFrequency(1);
                    newLemmaForDB.setSite(siteModel);
                } else {
                    Lemma currentLemmaInDB = lemmasOptional.get();
                    int lemmaId = currentLemmaInDB.getId();
                    newLemmaForDB = lemmaRepository.findById(lemmaId).get();
                    newLemmaForDB.setFrequency(newLemmaForDB.getFrequency() + 1);
                }

                session.saveOrUpdate(newLemmaForDB);
                lemmaArrayList.add(newLemmaForDB);

                writeToIndexEntity(page, newLemmaForDB, entry, session);
            }
            transaction.commit();
        }
        session.close();
    }

    public void writeToIndexEntity(Page page, Lemma newLemmaForDB, Map.Entry<String, Integer> entry, Session session) {

        IndexModel indexModel = new IndexModel();
        IndexModel.ComplexID complexID = new IndexModel.ComplexID(page.getId(), newLemmaForDB.getId());
        indexModel.setRank(Float.valueOf(entry.getValue()));
        indexModel.setComplexID(complexID);
        session.saveOrUpdate(indexModel);

    }

    public void writeLastError(Set<String> set, SiteModel site, String url) {

        int count = 0;
        for (String siteUrlInSet : set) {
            if (siteUrlInSet.contains(url)) {
                count++;
            }
        }

        if ((count == 0) && doStop()) {
            site.setStatus(Status.FAILED);
            site.setLastError("Индексация остановлена пользователем");
            siteRepository.save(site);
        }
    }

}
