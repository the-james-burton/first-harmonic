package com.firstharmonic.stocks;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Security {

    private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public final Date listDate;
    public final String name;
    public final String country;
    public final String region;
    public final String market;
    public final String fsaListingCategory;
    public final boolean ordShares;
    public final boolean depReceipts;
    public final boolean fixedInt;
    public final boolean warrants;
    public final String ISIN;
    public final String stockName;
    public final String EPIC;

    public Security(String line) throws ParseException {

        String[] fields = line.split("\t");

        if (!"".equals(fields[0])) {
            this.listDate = dateFormat.parse(fields[0]);
        } else {
            this.listDate = null;
        }
        this.name = fields[1].trim().replace("&", "&amp;").trim();;
        this.country = fields[2].trim();
        this.region = fields[3].trim();
        this.market = fields[4].trim();
        this.fsaListingCategory = fields[5];
        this.ordShares = fields[6].equals("YES") ? true : false;
        this.depReceipts = fields[7].equals("YES") ? true : false;
        this.fixedInt = fields[8].equals("YES") ? true : false;
        this.warrants = fields[9].equals("YES") ? true : false;
        this.ISIN = fields[10].trim();;
        this.stockName = fields[11].trim();;
        this.EPIC = fields[12].replace(".", "").trim();
    }

}
