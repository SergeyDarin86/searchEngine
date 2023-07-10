package searchengine.services;

import lombok.Getter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;


public class GetLemma {

    @Getter
    private static LuceneMorphology morphology;

    static {
        try {
            morphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTextFromHTML(String url) throws IOException {
        Document document = Jsoup.connect(url).timeout(3000).ignoreHttpErrors(true).get();
        return document.text();
    }

    public Map<String,Integer> getMapOfLemmasFromHTML(String url) throws IOException {

        String textFromHTML = getTextFromHTML(url);
        List<String> splitPhrase = getSplitPhrase(textFromHTML);
        List<String> lemmasListFromSomeText;
        HashMap<String, Integer> map = new HashMap<>();

        for (String word : splitPhrase) {
            lemmasListFromSomeText = getLemmasListFromSomeText(word);
            for (String cleanedLemma : lemmasListFromSomeText) {
                if (map.containsKey(cleanedLemma)) {
                    map.put(cleanedLemma, map.get(cleanedLemma) + 1);
                } else {
                    map.put(cleanedLemma, 1);
                }
            }
        }
        return map;

    }

    public static List<String> getSplitPhrase(String someText) {
        return List.of(someText.trim().split("[^а-яА-Я]+"));
    }

    public static List<String> getLemmasListFromSomeText(String word) {
        List<String> lemmaListFromSomeText = new ArrayList<>();
        if (!word.equals("")) {
            List<String> wordBaseForms = morphology.getMorphInfo(word.toLowerCase());
            for (String lemma : wordBaseForms) {
                String cleanedLemma = lemma.substring(0, lemma.indexOf('|'));
                boolean isLemmaContain = !(lemma.contains("СОЮЗ") || lemma.contains("ПРЕДЛ") || lemma.contains("ЧАСТ") || lemma.contains("МЕЖД"));
                if (isLemmaContain && ((cleanedLemma.length() != 1) || cleanedLemma.equals("я"))) {
                    lemmaListFromSomeText.add(cleanedLemma);
                }
            }
        }

        return lemmaListFromSomeText;
    }

}
