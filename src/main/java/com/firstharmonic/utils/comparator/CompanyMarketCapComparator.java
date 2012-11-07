package com.firstharmonic.utils.comparator;

import java.util.Comparator;

import com.firstharmonic.stocks.Company;

public class CompanyMarketCapComparator implements Comparator<Company> {

    @Override
    public int compare(Company o1, Company o2) {
        return new Float(o2.marketCap).compareTo(o1.marketCap);
    }


}
