package com.firstharmonic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import com.firstharmonic.stocks.Company;
import com.firstharmonic.stocks.Security;

/**
 * http://www.londonstockexchange.com/statistics/companies-and-issuers/companies-and-issuers.htm http://www.reuters.com/finance/stocks/ratios
 * 
 * @author the.james.burton
 */
public class CreateDownloadLinks {
    private static Logger                    logger         = Logger.getLogger(CreateDownloadLinks.class.getName());
    private static final String              outputPath     = "c:/dev/first-harmonic/results";
    private static final String              companyFile    = outputPath + "/companies.txt";
    private static final String              securityFile   = outputPath + "/securities.txt";
    private static final String              templatePath   = "c:/dev/first-harmonic/templates";
    private static final String              ratioLink      = "http://www.reuters.com/finance/stocks/ratios?symbol=";
    private static final String              ratioChartLink = "http://www.reuters.com/charts/cr/?display=mountain&width=172&height=124&frequency=1week&duration=1year&symbol=";
    private static HashMap<String, Company>  companies      = new HashMap<String, Company>();
    private static HashMap<String, Security> securities     = new HashMap<String, Security>();
    private static List<String>              epics          = new ArrayList<String>();
    private static VelocityEngine            ve;

    public static void main(String args[]) throws Exception {
        importCompanies(companyFile);
        importSecurities(securityFile);
        runReports();
    }

    private static void importCompanies(String filename) {
        logger.info("importing companies list");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            line = reader.readLine(); // ignore headers
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line.trim())) {
                    Company company = new Company(line);
                    if (company.ordShares) {
                        companies.put(company.name, company);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("number of companies: " + String.valueOf(companies.size()));
    }

    private static void importSecurities(String filename) {
        logger.info("importing securities list");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            line = reader.readLine(); // ignore headers
            while ((line = reader.readLine()) != null) {
                if (!"".equals(line)) {
                    Security security = new Security(line);
                    if (security.ordShares && security.stockName.startsWith("ORD") && companies.containsKey(security.name)) {
                        securities.put(security.EPIC, security);
                        epics.add(security.EPIC);
                        companies.get(security.name).setMarket(security.market);
                        companies.get(security.name).setCountry(security.country);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("number of securities: " + String.valueOf(securities.size()));
    }

    private static void runReports() throws Exception {
        ve = new VelocityEngine();
        // ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);
        ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templatePath);
        ve.init();
        VelocityContext context = new VelocityContext();
        //
        context.put("link", ratioLink);
        context.put("epics", epics);
        logger.info("generating ratio-links.html");
        velocityReport("links.tmpl", new File(outputPath, "ratios-links.html"), context);
        //
        context.put("link", ratioChartLink);
        logger.info("generating chart-links.html");
        velocityReport("links.tmpl", new File(outputPath, "chart-links.html"), context);
    }

    private static void velocityReport(String transform, File file, VelocityContext context) throws Exception {
        // --- VELOCITY SETUP ---
        Template template = null;
        template = ve.getTemplate(transform);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        FileUtils.writeStringToFile(file, sw.toString());
    }
}
