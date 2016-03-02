package tum_model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WebPageDb {
    private static WebPageDb mInstance;
    public static final String WEB_REQUESTED_ID_PROPERTY = "RequestedPageId";
    public static final String WEB_PAGE_PROPERTY = "WebPage";

    private List<WebPage> webPages;
    private ZipfGenerator zipf;

    private WebPageDb() {
    }

    public static WebPageDb getInstance() {
        if (mInstance == null) {
            mInstance = new WebPageDb();
        }
        return mInstance;
    }

    public static void initWebPageDb(int pageCount, int minSize, int maxSize) {
        getInstance().initInternal(pageCount,minSize,maxSize);
    }

    private void initInternal(int pageCount, int minSize, int maxSize) {
        zipf = new ZipfGenerator(pageCount, 1);
        webPages = new ArrayList<>(pageCount);
        for (int i=0; i<pageCount; i++) {
            WebPage newPage = new WebPage();
            newPage.id = i;
            newPage.size = ThreadLocalRandom.current().nextInt(minSize,maxSize);
            webPages.add(newPage);
        }
    }

    public WebPage getPageById(int id) {
        return webPages.get(id);
    }

    public WebPage getRandomPage()
    {
        return webPages.get(zipf.next());
    }

    public int getRandomPageId() {
        return zipf.next();
    }
}
