package searchengine.services;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MainSearch {

    SitesList sitesList;

    PageRepository pageRepository;

    LemmaRepository lemmaRepository;

    IndexRepository indexRepository;

    SiteRepository siteRepository;


    public MainSearch(PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, SiteRepository siteRepository, SitesList sitesList) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.siteRepository = siteRepository;
        this.sitesList = sitesList;
    }

    private final HashMap<String, Integer> unsortedMapByFrecuency = new HashMap<>();
    private Map<String, Integer> sortedMapByFrecuency;
    private final HashMap<Page, Float> unSortedMapByRelevance = new HashMap<>();

    @Getter
    private static Map<Page, Float> sortedMapByRelevance;

    @Getter
    private static int countOfPages;

    private int siteId;

    private static final int SNIPPET_LENGTH = 200;

    private String newSnippet;
    private int startSnippetFirst = 0;
    private int endSnippetLast = 0;
    private int middleStartPositionForSnippet = 0;
    private int middleEndPositionForSnippet = 0;
    private int startWordPosition = 0;
    private int endWordPosition;
    private int endPositionPreviousWord = 0;
    private int count = 0;

    public void searchPages(String query, String site) {

        List<String> splitPhraseFromQuery = GetLemma.getSplitPhrase(query);
        List<String> lemmaListFromQuery = getListFromQuery(splitPhraseFromQuery);

        lemmaListFromQuery.forEach(cleanedLemma -> {
            if (!isNullOrEmptySiteFromPopUpList(site)) {
                SiteModel siteModel = siteRepository.siteByName(site);
                siteId = siteModel.getId();
                Optional<Lemma> lemmaOptional = lemmaRepository.findByLemmaAndSiteId(cleanedLemma, siteId);
                if (lemmaOptional.isPresent()) {
                    Lemma lemmaFromDB = lemmaOptional.get();
                    unsortedMapByFrecuency.put(lemmaFromDB.getLemma(), lemmaFromDB.getFrequency());
                }
            } else {
                putLemmasToMapByCommonFrequencyNew(cleanedLemma);
            }
        });

        if (!isNullOrEmptyMap(getSortedMapByFrequency())) {

            Map.Entry firstValue = sortedMapByFrecuency.entrySet().stream().findFirst().get();
            List<Lemma> lemmaListFromDbByName;
            if (!isNullOrEmptySiteFromPopUpList(site)) {
                lemmaListFromDbByName = lemmaRepository.lemmasFromDBbyNameAndSite(firstValue.getKey().toString(), siteId);
            } else {
                lemmaListFromDbByName = lemmaRepository.lemmasFromDB(firstValue.getKey().toString());
            }
            fillingUnSortedMapByRelevance(retainPagesList(lemmaListFromDbByName));
        }
        getSortedListOfPagesByRelevance(unSortedMapByRelevance);

    }

    public static boolean isNullOrEmptyMap(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isNullOrEmptySiteFromPopUpList(String site) {
        return (site == null || site.isEmpty());
    }

    public static String getTextFromPageContent(String content) {
        Document document = Jsoup.parse(content);
        return document.text();
    }

    public static List<String> getListFromQuery(List<String> splitPhraseFromQuery) {
        List<String> lemmaListFromQuery = new ArrayList<>();
        for (String w : splitPhraseFromQuery) {
            lemmaListFromQuery.addAll(GetLemma.getLemmasListFromSomeText(w));
        }
        return lemmaListFromQuery;
    }

    public void putLemmasToMapByCommonFrequencyNew(String cleanedLemma) {

        List<Lemma> lemmaList = lemmaRepository.lemmasFromDB(cleanedLemma);
        if (!(lemmaList.size() == 0)) {
            int commonFrequency = lemmaRepository.commonFrequencyForAllSites(cleanedLemma);
            unsortedMapByFrecuency.put(cleanedLemma, commonFrequency);
        }

    }

    public List<Page> retainPagesList(List<Lemma> lemmaListFromDbByName) {
        List<Page> pageListForFirstLemma = new ArrayList<>();

        for (Lemma firstLemmaFromDB : lemmaListFromDbByName) {
            pageListForFirstLemma.addAll(pageRepository.pagesListByLemmaID(firstLemmaFromDB.getId()));
        }

        for (Map.Entry<String, Integer> entry : sortedMapByFrecuency.entrySet()) {
            String lemmaName = entry.getKey();
            pageListForFirstLemma.retainAll(pageRepository.pagesListByLemmaName(lemmaName));
        }
        return pageListForFirstLemma;
    }

    public void fillingUnSortedMapByRelevance(List<Page> pageListForFirstLemma) {

        List<Float> listRelevance = new ArrayList<>();

        for (Page page : pageListForFirstLemma) {
            float absRelevance = 0;
            for (Map.Entry<String, Integer> entry : sortedMapByFrecuency.entrySet()) {
                String lemmaName = entry.getKey();
                for (IndexModel indexModel : page.getIndexModels()) {
                    if (indexModel.getLemma().getLemma().equals(lemmaName)) {
                        absRelevance += indexModel.getRank();
                    }
                }
            }
            listRelevance.add(absRelevance);
            float maxRelevance = Collections.max(listRelevance);
            float relativeRelevance = absRelevance / maxRelevance;
            unSortedMapByRelevance.put(page, relativeRelevance);
        }
    }

    public Map<String, Integer> getSortedMapByFrequency() {
        sortedMapByFrecuency = unsortedMapByFrecuency.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
        return sortedMapByFrecuency;
    }

    public void getSortedListOfPagesByRelevance(HashMap<Page, Float> unSortedMapByRelevance) {

        sortedMapByRelevance = unSortedMapByRelevance.entrySet().stream()
                .sorted(Comparator.comparingDouble(e -> -e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
        countOfPages = sortedMapByRelevance.size();

    }

    public String getPageTitle(String content) {
        Pattern p = Pattern.compile("<head>.*?<title>(.*?)</title>.*?</head>", Pattern.DOTALL);
        Matcher m = p.matcher(content);

        String title = "";
        while (m.find()) {
            title = m.group(1);
        }
        return title;
    }

    public String getSnippet(Page page, String query) throws IOException {

        String text = getTextFromPageContent(page.getContent());
        List<String> splitPhraseFromText = GetLemma.getSplitPhrase(text);
        List<String> splitPhraseFromQuery = GetLemma.getSplitPhrase(query);
        List<String> lemmasListFromQuery = new ArrayList<>();
        List<Snippet> snippetList = new ArrayList<>();

        for (String w : splitPhraseFromQuery) {
            lemmasListFromQuery.addAll(GetLemma.getLemmasListFromSomeText(w));
        }

        newSnippet = mainLoopWithSnippet(splitPhraseFromText, text, lemmasListFromQuery, splitPhraseFromQuery, snippetList);

        return newSnippet;
    }

    public String mainLoopWithSnippet(List<String> splitPhraseFromText, String text,
                                      List<String> lemmasListFromQuery, List<String> splitPhraseFromQuery, List<Snippet> snippetList) {
        for (int k = 0; k < splitPhraseFromText.size(); k++) {
            String word = splitPhraseFromText.get(k);
            startWordPosition = (k == 0) ? text.indexOf(word) : text.indexOf(word, startWordPosition);
            endWordPosition = startWordPosition + splitPhraseFromText.get(k).length();
            List<String> lemmasListFromSomeText = GetLemma.getLemmasListFromSomeText(word);

            for (String s : lemmasListFromSomeText) {
                for (int i = 0; i < lemmasListFromQuery.size(); i++) {
                    if (s.equalsIgnoreCase(lemmasListFromQuery.get(i))) {
                        int difference = 0;
                        if (splitPhraseFromQuery.size() == 1) {
                            startSnippetFirst = startWordPosition;
                            endSnippetLast = endWordPosition;
                        } else {
                            ifIEqualsZero(i);
                            if (i != 0) {
                                difference = startWordPosition - endPositionPreviousWord;
                            }
                            ifDifferenceMoreThanOne(difference);
                            ifDifferenceEqualsOne(difference, i);
                            ifDifferenceNotEqualsOneAnd(difference, i, lemmasListFromQuery);
                            count++;
                            getSnippetObject(snippetList, count, startSnippetFirst, endSnippetLast, i, word, startWordPosition, endWordPosition, difference);
                            endPositionPreviousWord = endWordPosition;
                        }
                    }
                }
            }
        }
        return newSnippet = finalProcessOfGettingSnippet(snippetList, startSnippetFirst, endSnippetLast, text);
    }

    public void ifIEqualsZero(int i) {
        if (i == 0) {
            startSnippetFirst = startWordPosition;
            middleStartPositionForSnippet = startWordPosition;
            count = 0;
        }
    }

    public void ifDifferenceMoreThanOne(int difference) {
        if (difference > 1) {
            startSnippetFirst = startWordPosition;
            count = 0;
            middleStartPositionForSnippet = startWordPosition;
            middleEndPositionForSnippet = endWordPosition;
        }
    }

    public void ifDifferenceEqualsOne(int difference, int i) {
        if (difference == 1 || i == 0) {
            endSnippetLast = endWordPosition;
            middleEndPositionForSnippet = endWordPosition;
        } else {
            startSnippetFirst = startWordPosition;
        }
    }

    public void ifDifferenceNotEqualsOneAnd(int difference, int i, List<String> lemmasListFromQuery) {
        if (difference != 1 && (i == (lemmasListFromQuery.size() - 1)) || (endSnippetLast - startSnippetFirst < 1)) {
            startSnippetFirst = middleStartPositionForSnippet;
            endSnippetLast = middleEndPositionForSnippet;
        }
    }

    public void getSnippetObject(List<Snippet> snippetList, int count, int startSnippetFirst, int endSnippetLast, int i, String word,
                                 int startWordPosition, int endWordPosition, int difference) {
        Snippet snippet = new Snippet();
        snippet.setCountWords(count);
        snippet.setStartSnippet(startSnippetFirst);
        snippet.setEndSnippet(endSnippetLast);
        snippet.setIPosition(i);
        snippet.setWord(word);
        snippet.setStartWord(startWordPosition);
        snippet.setEndWord(endWordPosition);
        snippet.setDifference(difference);
        snippetList.add(snippet);
    }

    public String finalProcessOfGettingSnippet(List<Snippet> snippetList, int startSnippetFirst, int endSnippetLast, String text) {
        String newSnippet;
        try {
            int max = snippetList.get(0).getCountWords();
            for (Snippet snippet : snippetList) {
                if (snippet.getCountWords() > max) {
                    max = snippet.countWords;
                    startSnippetFirst = snippet.startSnippet;
                    endSnippetLast = snippet.endSnippet;
                }
            }
        } catch (IndexOutOfBoundsException ignored) {

        }

        int endPlusSnippetLength = endSnippetLast + SNIPPET_LENGTH;

        if (text.length() >= endPlusSnippetLength) {
            newSnippet = "<b>" + text.substring(startSnippetFirst, endSnippetLast) + "</b>" + text.substring(endSnippetLast, endPlusSnippetLength) + "...";
        } else {
            int difference = text.length() - endSnippetLast;
            int remains = SNIPPET_LENGTH - difference;
            newSnippet = "... " + text.substring(startSnippetFirst - remains, startSnippetFirst) + "<b>" + text.substring(startSnippetFirst, endSnippetLast) + "</b>" + text.substring(endSnippetLast);
        }
        return newSnippet;
    }

    @Getter
    @Setter
    static class Snippet {

        private String word;
        private int countWords;
        private int startSnippet;
        private int endSnippet;
        private int iPosition;
        private int startWord;
        private int endWord;
        private int difference;

    }

}
