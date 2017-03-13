package com.a6v.tjreader.services;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class ArticleHtmlParser {
  private final Element article;

  public ArticleHtmlParser(Document document) {
    article = document.getElementsByTag("article").first();
  }

  public List<String> findImageUrls() {
    ArrayList<String> urls = new ArrayList<>();
    for (Element img : article.getElementsByTag("img")) {
      String src = img.attr("src");
      if (!TextUtils.isEmpty(src)) {
        urls.add(src);
      }
    }
    return urls;
  }

  public static String getIntroText(String introHtml) {
    return Jsoup.parse(introHtml).text();
  }
}
