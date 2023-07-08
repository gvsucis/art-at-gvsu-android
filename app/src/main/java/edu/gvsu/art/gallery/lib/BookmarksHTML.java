package edu.gvsu.art.gallery.lib;

public class BookmarksHTML {
    private static final String DOCTYPE =
            "<!DOCTYPE NETSCAPE-Bookmark-file-1>";
    private static final String METADATA =
            "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">";

    private final String html;

    private BookmarksHTML(Builder builder) {
        html = builder.html;
    }

    public String getHTML() {
        return html;
    }

    public static class Builder {
        private String html;

        public Builder(String formattedTime) {
            html = String.format("%s\n%s\n<h3>Favorites %s</h3>\n<dl><p>", DOCTYPE,
                    METADATA, formattedTime);
        }

        public Builder append(String title, String link) {
            html += String.format("\n\t<dt><a href=\"%s\">%s</a></dt>", link, title);
            return this;
        }

        public BookmarksHTML build() {
            html += "\n</dl><p>";
            return new BookmarksHTML(this);
        }
    }
}
