package com.firstharmonic.data;


public enum Ratio {

    peRatio("P/E Ratio (TTM)", true),
    //peHigh5Years("P/E High - Last 5 Yrs.", true),
    //peLow5Years("P/E Low - Last 5 Yrs.", true),
    beta("Beta", false),
    priceToSales("Price to Sales (TTM)", true),
    priceToBook("Price to Book (MRQ)", true),
    priceToTangibleBook("Price to Tangible Book (MRQ)", true),
    priceToCashFlow("Price to Cash Flow (TTM)", true),
    priceToFreeCashFlow("Price to Free Cash Flow (TTM)", true),

    quickRatio("Quick Ratio (MRQ)", false),
    currentRatio("Current Ratio (MRQ)", false),
    ltDebtToEquity("LT Debt to Equity (MRQ)", true),
    totalDebtToEquity("Total Debt to Equity (MRQ)", true),
    //interestCoverage("Interest Coverage (TTM)", false),

    grossMargin("Gross Margin (TTM)", false),
    grossMargin5YearAverage("Gross Margin - 5 Yr. Avg.", false),
    EBITDMargin("EBITD Margin (TTM)", false),
    EBITD5yearAverage("EBITD - 5 Yr. Avg", false),
    operatingMargin("Operating Margin (TTM)", false),
    operatingMargin5YearAverage("Operating Margin - 5 Yr. Avg.", false),
    preTaxMargin("Pre-Tax Margin (TTM)", false),
    preTaxMargin5YearAverage("Pre-Tax Margin - 5 Yr. Avg.", false),
    netProfitMargin("Net Profit Margin (TTM)", false),
    netProfitMargin5YearAverage("Net Profit Margin - 5 Yr. Avg.", false),
    effectiveTaxRate("Effective Tax Rate (TTM)", true),
    effectiveTaxRate5YearAverage("Effecitve Tax Rate - 5 Yr. Avg.", true),

    salesGrowthQuarter("Sales (MRQ) vs Qtr. 1 Yr. Ago", false),
    salesGrowth1Year("Sales (TTM) vs TTM 1 Yr. Ago", false),
    salesGrowth5Year("Sales - 5 Yr. Growth Rate", false),
    epsGrowthQuarter("EPS (MRQ) vs Qtr. 1 Yr. Ago", false),
    epsGrowth1Year("EPS (TTM) vs TTM 1 Yr. Ago", false),
    epsGrowth5Year("EPS - 5 Yr. Growth Rate", false),
    capitalSpendingGrowth5year("Capital Spending - 5 Yr. Growth Rate", false),

    revenuePerEmployee("Revenue/Employee (TTM)", false),
    netIncomePerEmployee("Net Income/Employee (TTM)", false),
    receivableTurnover("Receivable Turnover (TTM)", false),
    inventoryTurnover("Inventory Turnover (TTM)", false),
    assetTurnover("Asset Turnover (TTM)", false),

    returnOnAssets("Return on Assets (TTM)", false),
    returnOnAssets5YearAverage("Return on Assets - 5 Yr. Avg.", false),
    returnOnInvetment("Return on Investment (TTM)", false),
    returnOnInvestment5YearAverage("Return on Investment - 5 Yr. Avg.", false),
    returnOnEquity("Return on Equity (TTM)", false),
    returnOnEquity5YearAverage("Return on Equity - 5 Yr. Avg.", false),

    //dividendYield("Dividend Yield", false),
    dividendYield5YearAverage("Dividend Yield - 5 Year Avg.", false),
    dividendYield5YearGrowthRate("Dividend 5 Year Growth Rate", false),
    payoutRatio("Payout Ratio(TTM)", true);

    private Statistics statistics;
    private final String name;
    private final boolean lowGood;

    Ratio(String name, boolean isLowGood) {
        this.name = name;
        this.lowGood = isLowGood;
        statistics = new Statistics(lowGood);
        
    }

    public String getName() {
        return name;
    }

    public boolean isLowGood() {
        return lowGood;
    }

    public Statistics getStatistics() {
        return statistics;
    }
    
}
