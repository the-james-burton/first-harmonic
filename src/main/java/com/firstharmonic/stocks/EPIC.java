package com.firstharmonic.stocks;

import java.util.HashMap;

import com.firstharmonic.data.Ratio;

public class EPIC {

    private final HashMap<String, Float> ratios = new HashMap<String, Float>();
    private final String name;

    public EPIC(String name, String html) {
        this.name = name;
        for (Ratio ratio : Ratio.values()) {
            Float value = discoverRatio(html, ratio.getName());
            if (value != null) {
                ratios.put(ratio.toString(), value);
            }
        }
        System.out.println(toString());

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String ratio : ratios.keySet()) {
            sb.append(ratio + ":" + ratios.get(ratio) + ", ");
        }
        return "[" + name + ":: " + sb.toString() + "]";
    }

    private Float discoverRatio(String html, String ratio) {
        Float result = null;
        int begin = html.indexOf(ratio);
        if (begin > -1) {
            int start = html.substring(begin).indexOf("<td class=\"data\">") + 17;
            int end = html.substring(begin + start).indexOf("</td>") + start;
            try {
                result = Float.parseFloat(html.substring(begin + start, begin + end));
            } catch (Exception e) {
                // ignore unparseable value or other error
            }
        }
        return result;
    }

    public HashMap<String, Float> getRatios() {
        return ratios;
    }

    public String getName() {
        return name;
    }
    
}
