package com.firstharmonic;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.firstharmonic.stocks.EPIC;
import com.firstharmonic.stocks.HTML;

public class Parser implements Callable<EPIC> {
    private static Logger logger = Logger.getLogger(Parser.class.getName());

    private Future<HTML> future;
    
    public Parser(Future<HTML> future) {
        this.future = future;
    }
    
    @Override
    public EPIC call() throws Exception {
        //HTML html = Analyse.getDownload();
        HTML html = future.get();
        String ric = html.getRic();
        logger.info(ric + ":parsing");
        return new EPIC(ric, html.getHtml());
        }
}
