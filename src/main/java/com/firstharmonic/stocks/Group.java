package com.firstharmonic.stocks;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.firstharmonic.data.Ratio;
import com.firstharmonic.data.Statistics;

public class Group {

    private Map<Ratio, Statistics> statistics = new HashMap<Ratio, Statistics>();
    private final Statistics ranking = new Statistics(false);
    private final String name;

    private final List<Company> companies;
    private final HashMap<String, Integer> rankings = new HashMap<String, Integer>();

    public Group(String name, List<Company> companies) {
        this.name = name;
        this.companies = companies;
        for (Ratio ratio : Ratio.values()) {
            statistics.put(ratio, new Statistics(ratio.isLowGood()));
        }
    }

    public String getName() {
        return name;
    }

    public Map<Ratio, Statistics> getStatistics() {
        return statistics;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    @SuppressWarnings("unchecked")
    public void sortCompanies(Comparator comparator) {
        Collections.sort(companies, comparator);
    }

    public Statistics getRanking() {
        return ranking;
    }

    public void rankCompanies() {
        for (Company company : companies) {
            int total = 0;
            EPIC epic = company.getEpic();
            if (epic != null) {
                for (Ratio ratio : Ratio.values()) {
                    Float value = epic.getRatios().get(ratio.toString());
                    if (value != null) {
                        total = total + statistics.get(ratio).getRating(value);
                    }
                }
                ranking.addValue(total);
                rankings.put(epic.getName(), total);
                company.setRanking(total);
            }
        }
    }

    public void setCompanyRankings() {
        for (Company company : companies) {
            EPIC epic = company.getEpic();
            if (epic != null) {
                company.setRanking(rankings.get(company.getEpic().getName()));
            }
        }
    }

}
