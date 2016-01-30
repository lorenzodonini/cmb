package tum_model;

import tum_model.WebPage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WebPageDb {

    public WebPageDb(int pageCount, int minSize, int maxSize) {
        this.zipf = new ZipfGenerator(pageCount, 1);

        webPages = new ArrayList<>(pageCount);
        for(int i = 0; i < webPages.size(); ++i) {
            WebPage newPage = new WebPage();
            newPage.id = i;
            newPage.size = ThreadLocalRandom.current().nextInt(minSize, maxSize);
            webPages.set(i, newPage);
        }
    }


    public WebPage getRandomPage()
    {
        return webPages.get(zipf.next());
    }


    private List<WebPage> webPages;
    private ZipfGenerator zipf;

}
