package com.firstharmonic.utils.comparator;

import java.util.Comparator;

import com.firstharmonic.stocks.Company;

public class CompanyRankingComparator implements Comparator<Company> {

    @Override
    public int compare(Company o1, Company o2) {
        Integer r1 = o1.getRanking() == null ? 0 : o1.getRanking();
        Integer r2 = o2.getRanking() == null ? 0 : o2.getRanking();

        return r2.compareTo(r1);
    }

}
