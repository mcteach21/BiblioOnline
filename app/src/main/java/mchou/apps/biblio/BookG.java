package mchou.apps.biblio;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class BookG implements Serializable {
    private String id;
    private String authors;
    private String title;
    private String subtitle;
    private String description;
    private String publisher;
    private String publishedDate;
    private String pageCount;

    public static ArrayList<BookG> fromJson(JSONArray jsonArray) {
        ArrayList<BookG> books = new ArrayList<BookG>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject bookJson = null;
            try {
                bookJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            BookG book = BookG.fromJson(bookJson);
            if (book != null) {
                books.add(book);
            }
        }
        return books;
    }
    private static BookG fromJsonBook(JSONObject jsonObject) {
        BookG book = new BookG();
        try {

            book.title = jsonObject.getString("title");
            //book.authors = jsonObject.getString("authors");

            JSONArray authors_array = jsonObject.getJSONArray("authors");
            int nb = authors_array.length();
            final String[] authors = new String[nb];
            for (int i = 0; i < nb; ++i) {
                authors[i] = authors_array.getString(i);
            }
            book.authors = TextUtils.join(", ", authors);

            book.subtitle = jsonObject.has("subtitle") ?jsonObject.getString("subtitle"):"";
            book.description = jsonObject.has("description") ? jsonObject.getString("description"):"";

            book.publisher = jsonObject.has("publisher")?jsonObject.getString("publisher"):"?";
            book.publishedDate = jsonObject.has("publishedDate")?jsonObject.getString("publishedDate"):"?";

            book.pageCount = jsonObject.has("pageCount")?jsonObject.getString("pageCount"):"0";

        } catch (JSONException e) {
            Log.i("Async", "fromJson: Error = "+e);
        }
        return book;
    }
    public static BookG fromJson(JSONObject jsonObject) {
        BookG book = new BookG();

        try {
            JSONObject infos = jsonObject.getJSONObject("volumeInfo");
            Log.i("Async", "fromJson: infos = "+infos);

            book = fromJsonBook(infos);

            book.id = jsonObject.getString("id");
        } catch (JSONException e) {
            Log.i("Async", "fromJson: Error = "+e);
        }
        return book;
    }
    private static String getAuthors(final JSONObject jsonObject) {
        try {
            final JSONArray authors = jsonObject.getJSONArray("authors");
            int numAuthors = authors.length();
            final String[] authorStrings = new String[numAuthors];
            for (int i = 0; i < numAuthors; ++i) {
                authorStrings[i] = authors.getString(i);
            }
            return TextUtils.join(", ", authorStrings);
        } catch (JSONException e) {
            return "";
        }
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getPageCount() {
        return pageCount;
    }

    public void setPageCount(String pageCount) {
        this.pageCount = pageCount;
    }

    public String getCoverUrl() {
        return "http://books.google.com/books/content?id=" + id + "&printsec=frontcover&img=1&zoom=5&source=gbs_api";
    }
    public String getLargeCoverUrl() {
        return "http://books.google.com/books/content?id=" + id + "&printsec=frontcover&img=1&zoom=1&source=gbs_api";
    }
    @Override
    public String toString() {
        return "[" +
                "id='" + id + '\'' +
                ", authors='" + authors + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", description='" + description + '\'' +
                ']';

    }

    /* public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String DESCRIPTION = "description";
    public static final String PUBLISHER = "publisher";
    public static final String AUTHORS = "authors";
    public static final String LIST_PRICE = "list_price";
    public static final String RETAIL_PRICE = "retail_price";
    public static final String IMAGE = "image";
    public static final String RETAIL_PRICE_CURRENCY_CODE = "retail_price_currency_code";
    public static final String LIST_PRICE_CURRENCY_CODE = "list_price_currency_code";
    public static final String PUBLISHED_DATE = "published_date";*/
}
