package com.firstharmonic.data;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Statistics {

    private final boolean lowGood;
    private DescriptiveStatistics statistics = new DescriptiveStatistics();

    public Statistics(boolean isLowGood) {
        this.lowGood = isLowGood;
    }

    public void addValue(double value) {
        statistics.addValue(value);
    }

    public double getStandardDeviation() {
        return statistics.getStandardDeviation();
    }

    public double getMedian() {
        return statistics.getPercentile(50);
    }

    /*
     * public Double getMean() { return statistics.getMean(); }
     * 
     * public double getMin() { return statistics.getMin(); }
     * 
     * public double getMax() { return statistics.getMax(); }
     */

    public double getDistance(double value) {
        double sd = statistics.getStandardDeviation();
        double mean = statistics.getMean();
        double distance = (value - mean);
        double rating = distance / sd;
        return rating;
    }

    public int getRating(double value) {

        if (statistics.getN() < 2) {
            return 0;
        }

        double min = statistics.getMin();
        double max = statistics.getMax();

        // assert min != Double.NaN;
        // assert max != Double.NaN;
        // assert value >= min;
        // assert value <= max;

        if (!(min != Double.NaN) || !(max != Double.NaN) || !(value >= min) || !(value <= max)) {
            System.out.println("getRating called in error");
            return 0;
        }

        double median = statistics.getPercentile(50);
        double rating = 0;

        if (value < median) {
            rating = (((value - min) / (median - min) * 255)) - 255;
        } else {
            rating = ((value - median) / (max - median)) * 255;
        }

        if (rating == Double.NaN) {
            rating = 0;
        }

        if (lowGood) {
            rating = -rating;
        }

        return (int) rating;
    }

    public String getRatingColor(double value) {
        int rating = getRating(value);
        int red = 255;
        int green = 255;
        int blue = 255;

        if (rating < 0) {
            green = green + rating;
            blue = blue + rating;
        } else {
            red = red - rating;
            blue = blue - rating;
        }

        // assert 0 <= red && red <= 255;
        // assert 0 <= green && green <= 255;
        // assert 0 <= blue && blue <= 255;

        if (!(0 <= red && red <= 255) || !(0 <= green && green <= 255) || !(0 <= blue && blue <= 255)) {
            System.out.println("getRatingColor called in error");
            return "ffffff";

        }

        StringBuffer sb = new StringBuffer();
        String sRED = Integer.toHexString(red);
        String sGREEN = Integer.toHexString(green);
        String sBLUE = Integer.toHexString(blue);

        sb.append(("0" + sRED).substring(sRED.length() - 1));
        sb.append(("0" + sGREEN).substring(sGREEN.length() - 1));
        sb.append(("0" + sBLUE).substring(sBLUE.length() - 1));

        return sb.toString();
    }

}
