package com.firstharmonic.utils.comparator;

import java.util.Comparator;

import com.firstharmonic.stocks.Company;

public class CompanySectorComparator implements Comparator<Company> {

    @Override
    public int compare(Company o1, Company o2) {
        return o1.sector.compareTo(o2.sector);
    }


}
