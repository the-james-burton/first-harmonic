package com.firstharmonic;

import java.util.logging.Logger;

import com.firstharmonic.stocks.EPIC;
import com.firstharmonic.stocks.HTML;

public class Parser implements Runnable {
    private static Logger logger = Logger.getLogger(Parser.class.getName());

    @Override
    public void run() {
        HTML html = Analyse.getDownload();
        String ric = html.getRic();
        logger.info(ric + ":parsing");
        EPIC epic = new EPIC(ric, html.getHtml());
        Analyse.putParsed(ric, epic);
    }
}
