package com.firstharmonic.data;


public enum Performance {

    week4("4 Week"),
    week13("13 Week"),
    week26("26 Week"),
    week52("52 Week"),
    ytd("YTD");

    private final String name;

    Performance(String name) {
        this.name = name;        
    }

    public String getName() {
        return name;
    }

}
