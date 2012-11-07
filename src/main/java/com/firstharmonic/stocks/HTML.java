package com.firstharmonic.stocks;

public class HTML {
    private final String ric;
    private final String html;

    public HTML(String ric, String html) {
        this.ric = ric;
        this.html = html;
    }

    public String getRic() {
        return ric;
    }

    public String getHtml() {
        return html;
    }
}
