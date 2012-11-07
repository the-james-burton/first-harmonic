package com.firstharmonic.stocks;

import java.text.ParseException;
import java.util.Date;

import com.firstharmonic.Analyse;

public class Company {

    public final Date listDate;
    public final String name;
    public final int group;
    public final String sector;
    public final String subSector;
    public final String region;
    public final boolean ordShares;
    public final boolean depReceipts;
    public final boolean fixedInt;
    public final boolean warrants;
    public final float marketCap;
    public final boolean techMARK;
    
    public String country;
    public String market;

    private EPIC epic;
    private Integer ranking;

    public Company(String line) throws ParseException {

        String[] fields = line.split("\t");

        if (!"".equals(fields[0])) {
            Date parsed = null;
            try {
                parsed = Analyse.getDateFormat().parse(fields[0]);
            } catch (Exception e) {
                // ignore unparseable dates
            }
            this.listDate = parsed;
        } else {
            this.listDate = null;
        }
        this.name = fields[1].trim().replace("&", "&amp;").trim();;
        this.group = (int)Float.parseFloat(fields[2]);
        this.sector = fields[3].replace("\"", "").replace("&", "&amp;").trim();
        this.subSector = fields[4].trim();
        this.country = fields[5].trim();
        this.region = fields[6].trim();
        this.market = fields[7].trim();
        this.ordShares = fields[8].equals("YES") ? true : false;
        this.depReceipts = fields[9].equals("YES") ? true : false;
        this.fixedInt = fields[10].equals("YES") ? true : false;
        this.warrants = fields[11].equals("YES") ? true : false;
        this.marketCap = Float.parseFloat(fields[12]);
        if (fields.length > 13) {
            this.techMARK = fields[13].equals("techMARK") ? true : false;
        } else {
            this.techMARK = false;
        }
    }

    public void setEpic(EPIC epic) {
        this.epic = epic;
    }

    public EPIC getEpic() {
        return epic;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public String getMarket() {
        return market.replace("International Main Market", "IMM").replace("UK Main Market", "UK");
    }
    
    public void setMarket(String market) {
        this.market = market;
    }

    public float getMarketCap() {
        return marketCap;
    }

    public boolean isTechMARK() {
        return techMARK;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public Integer getRanking() {
        return ranking;
    }
    
    
    
}
