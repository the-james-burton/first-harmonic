package com.firstharmonic;

import com.firstharmonic.stocks.Company;
import com.firstharmonic.stocks.EPIC;
import com.firstharmonic.stocks.Security;

public class Share {

    private EPIC ratios;
    private Company company;
    private Security security;

    public Share (EPIC ratios, Company company, Security security) {
        this.ratios = ratios;
        this.company = company;
        this.security = security;

    }
}
