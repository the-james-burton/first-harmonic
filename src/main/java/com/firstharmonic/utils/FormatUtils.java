package com.firstharmonic.utils;

import java.text.DecimalFormat;

public class FormatUtils {

    private DecimalFormat format = new DecimalFormat("###0.00;(###0.00)");

    public FormatUtils() {

    }

    public String format(double value) {
        String result = null;
        try {
            result = format.format(value);
        } catch (NumberFormatException e) {
            // e.printStackTrace();
        }
        return result;

    }
    
    public String nbsp(String text) {
        try {
            return text.replace(" ", "&nbsp;");
        } catch (Exception e) {
            return null;
        }
    }
    
}
