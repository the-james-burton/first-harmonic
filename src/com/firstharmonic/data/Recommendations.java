package com.firstharmonic.data;


public enum Recommendations {

    buy("(1) BUY"),
    outperform("(2) OUTPERFORM"),
    hold("(3) HOLD"),
    underperform("(4) UNDERPERFORM"),
    sell("(5) SELL");

    private final String name;

    Recommendations(String name) {
        this.name = name;        
    }

    public String getName() {
        return name;
    }

}
