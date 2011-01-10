package com.firstharmonic.stocks.inspection;

import com.firstharmonic.stocks.Company;

public class SectorInspector implements Inspector {

    @Override
    public String getGroup(Company company) {
        return company.sector;
    }

}
