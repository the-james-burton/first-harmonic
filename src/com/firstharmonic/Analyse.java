package com.firstharmonic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.w3c.tidy.Tidy;

import com.firstharmonic.data.Ratio;
import com.firstharmonic.stocks.Company;
import com.firstharmonic.stocks.EPIC;
import com.firstharmonic.stocks.Group;
import com.firstharmonic.stocks.HTML;
import com.firstharmonic.stocks.Security;
import com.firstharmonic.stocks.inspection.Inspector;
import com.firstharmonic.stocks.inspection.SectorInspector;
import com.firstharmonic.stocks.inspection.SubSectorInspector;
import com.firstharmonic.utils.FileUtils;
import com.firstharmonic.utils.FormatUtils;
import com.firstharmonic.utils.ImageUtils;
import com.firstharmonic.utils.comparator.CompanyMarketCapComparator;
import com.firstharmonic.utils.comparator.CompanyRankingComparator;
import com.firstharmonic.utils.comparator.CompanySectorComparator;
import com.firstharmonic.utils.comparator.CompanySubSectorComparator;

/**
 * http://www.londonstockexchange.com/statistics/companies-and-issuers/companies-and-issuers.htm http://www.reuters.com/finance/stocks/ratios
 * 
 * @author the.james.burton
 */
public class Analyse {
    private static Logger                     logger     = Logger.getLogger(Analyse.class.getName());
    private static final String               USAGE      = "usage: java com.firstharmonic.Analyse -DbasePath=<basePath> -DoutputPath=<outputPath>\nbasePath: where the project is installed\noutputPath: where you want the results to go\n";
    private String                            basePath;
    private String                            outputPath;
    private String                            templatePath;
    private String                            resultsPath;
    private String                            ratiosDir;
    private String                            reportsPath;
    private String                            lseDir;
    private String                            companyURL;
    private String                            securityURL;
    private String                            companyFileMarker;
    private String                            securityFileMarker;
    private String                            companyXLS;
    private String                            securityXLS;
    private String                            companyFile;
    private String                            securityFile;
    private String                            ratioLink;
    private String                            ratioChartLink;
    private final static Queue<String>        rics       = new ConcurrentLinkedQueue<String>();
    private final static BlockingQueue<HTML>  downloads  = new LinkedBlockingQueue<HTML>();
    private final static Map<String, Company> companies  = new ConcurrentHashMap<String, Company>();
    private final Map<String, Security>       securities = new ConcurrentHashMap<String, Security>();
    private final static Map<String, String>  epicName   = new ConcurrentHashMap<String, String>();
    private static final Map<String, EPIC>    epics      = new ConcurrentHashMap<String, EPIC>();
    private final SortedMap<String, Group>    sectors    = new TreeMap<String, Group>();
    private final SortedMap<String, Group>    subSectors = new TreeMap<String, Group>();
    private VelocityEngine                    ve;
    private static DateFormat                 dateFormat;

    public static void main(String args[]) throws Exception {
        Analyse me = new Analyse();
        // take the parameters in...
        me.basePath = System.getProperty("basePath", null);
        me.outputPath = System.getProperty("outputPath", null);
        if ((me.basePath == null) || (me.outputPath == null)) {
            System.err.println(USAGE);
            System.exit(-1);
        }
        // setup the constants derived from the above paramters...
        me.templatePath = me.basePath + "/templates";
        me.resultsPath = me.outputPath + "/results";
        me.ratiosDir = me.resultsPath + "/ratios";
        me.reportsPath = me.resultsPath + "/reports";
        me.lseDir = me.resultsPath + "/lse";
        me.companyXLS = me.lseDir + "/companies.xls";
        me.securityXLS = me.lseDir + "/securities.xls";
        me.companyFile = me.lseDir + "/companies.txt";
        me.securityFile = me.lseDir + "/securities.txt";
        // take the properties file in...
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(me.basePath, "firstharmonic.properties")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // load the properties we need into constants...
        me.companyURL = properties.getProperty("companyURL");
        me.securityURL = properties.getProperty("securityURL");
        me.companyFileMarker = properties.getProperty("companyFileMarker");
        me.securityFileMarker = properties.getProperty("securityFileMarker");
        dateFormat = new SimpleDateFormat(properties.getProperty("dateFormat"));
        me.ratioLink = properties.getProperty("ratioLink");
        me.ratioChartLink = properties.getProperty("ratioChartLink");
        // create any require directories...
        new File(me.lseDir).mkdirs();
        new File(me.ratiosDir).mkdirs();
        new File(me.reportsPath).mkdirs();
        // begin main processing...
        me.downloadXLS();
        me.createRatioHeaders();
        me.importCompanies(me.companyFile);
        me.importSecurities(me.securityFile);
        me.createGroup(me.sectors, new CompanySectorComparator(), new SectorInspector());
        me.createGroup(me.subSectors, new CompanySubSectorComparator(), new SubSectorInspector());
        me.importRatios(me.ratiosDir);
        me.computeStatistics();
        me.sortCompaniesByGroup(me.sectors, new CompanyMarketCapComparator());
        me.sortCompaniesByGroup(me.sectors, new CompanyRankingComparator());
        me.runReports();
        // me.printDebug();
    }

    private void downloadXLS() throws Exception {
        logger.info("importing XLS");
        Downloader.downloadFile(companyURL, companyXLS);
        Downloader.downloadFile(securityURL, securityXLS);
        parseCompanies(companyXLS, companyFile, companyFileMarker);
        parseCompanies(securityXLS, securityFile, securityFileMarker);
    }

    private void parseCompanies(String input, String output, String marker) throws Exception {
        InputStream xls = new FileInputStream(input);
        Workbook wb = WorkbookFactory.create(xls);
        Sheet sheet = wb.getSheetAt(0);
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        char separator = '\t';
        for (Row row : sheet) {
            int previous = -1;
            if (!started) {
                started = isRowStart(row, marker);
            }
            if (started) {
                for (Cell cell : row) {
                    for (int i = 1; i < (cell.getColumnIndex() - previous); i++) {
                        sb.append(separator);
                    }
                    previous = cell.getColumnIndex();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            sb.append(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                sb.append(dateFormat.format(cell.getDateCellValue()));
                            } else {
                                sb.append(cell.getNumericCellValue());
                            }
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            sb.append(cell.getBooleanCellValue());
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            sb.append(cell.getCellFormula());
                            break;
                        default:
                    }
                    sb.append(separator);
                    previous = cell.getColumnIndex();
                }
                sb.append("\n");
            }
        }
        FileUtils.save(sb.toString(), new File(output));
    }

    private boolean isRowStart(Row row, String marker) {
        boolean result = false;
        Cell cell = row.getCell(0);
        if ((cell != null) && (cell.getCellType() == Cell.CELL_TYPE_STRING) && marker.equals(cell.getStringCellValue())) {
            result = true;
        }
        return result;
    }

    private void importCompanies(String filename) {
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

    private void importSecurities(String filename) {
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
                        epicName.put(security.EPIC, security.name);
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

    private void createRatioHeaders() throws Exception {
        logger.info("creating rotated ratio title headers");
        File images = new File(reportsPath, "images");
        FileUtils.deleteContents(images);
        images.mkdirs();
        for (Ratio ratio : Ratio.values()) {
            ImageUtils.rotate(ratio.getName(), new File(images, ratio.toString() + ".png"));
        }
    }

    private void createGroup(SortedMap<String, Group> group, Comparator<Company> comparator, Inspector inspector) {
        logger.info("creating group: " + comparator.getClass().getName() + ", " + inspector.getClass().getName());
        List<Company> sortedCompanies = new ArrayList<Company>(companies.values());
        Collections.sort(sortedCompanies, comparator);
        String name = inspector.getGroup(sortedCompanies.get(0));
        List<Company> groupCompanies = new ArrayList<Company>();
        for (Company company : sortedCompanies) {
            String value = inspector.getGroup(company);
            if (name.equals(value)) {
                groupCompanies.add(company);
            } else {
                Group grouping = new Group(name, groupCompanies);
                group.put(name, grouping);
                name = value;
                groupCompanies = new ArrayList<Company>();
            }
        }
    }

    public static String getRic() {
        return rics.poll();
    }

    public static void putDownload(String ric, String download) {
        try {
            downloads.put(new HTML(ric, download));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static HTML getDownload() {
        HTML result = null;
        try {
            result = downloads.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }

    public static void putParsed(String ric, EPIC epic) {
        epics.put(ric, epic);
        companies.get(epicName.get(ric)).setEpic(epic);
    }

    private void importRatios(String dir) {
        logger.info("importing ratio files");
        for (String key : securities.keySet()) {
            rics.add(key);
        }
        int size = rics.size();
        ExecutorService downloaders = Executors.newFixedThreadPool(4);
        ExecutorService parsers = Executors.newFixedThreadPool(4);
        for (int i = 0; i < size; i++) {
            downloaders.execute(new Downloader(ratioLink, ratiosDir));
            parsers.execute(new Parser());
        }
        downloaders.shutdown();
        parsers.shutdown();
        // Wait for all tasks to complete
        try {
            downloaders.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            parsers.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info("Interrupted while waiting for thread pool to shutdown");
        }
    }

    private void computeStatistics() {
        logger.info("computing statistics");
        for (Ratio ratio : Ratio.values()) {
            for (EPIC epic : epics.values()) {
                Float value = epic.getRatios().get(ratio.toString());
                if (value != null) {
                    ratio.getStatistics().addValue(value);
                }
            }
        }
        populateGroupStatistics(sectors);
        populateGroupStatistics(subSectors);
    }

    private void populateGroupStatistics(SortedMap<String, Group> group) {
        for (Group grouping : group.values()) {
            for (Company company : grouping.getCompanies()) {
                for (Ratio ratio : Ratio.values()) {
                    if (company.getEpic() != null) {
                        Float value = company.getEpic().getRatios().get(ratio.toString());
                        if (value != null) {
                            grouping.getStatistics().get(ratio).addValue(value);
                        }
                    }
                }
            }
            grouping.rankCompanies();
        }
    }

    private void sortCompaniesByGroup(SortedMap<String, Group> group, Comparator comparator) {
        for (Group grouping : group.values()) {
            grouping.setCompanyRankings();
            grouping.sortCompanies(comparator);
        }
    }

    private void runReports() throws Exception {
        ve = new VelocityEngine();
        // ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, this);
        ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templatePath);
        ve.init();
        VelocityContext context = new VelocityContext();
        context.put("format", new FormatUtils());
        context.put("epics", epics);
        context.put("ratios", Ratio.values());
        context.put("numberOfRatios", Ratio.values().length);
        context.put("ratioChartLink", ratioChartLink);
        context.put("ratioLink", ratioLink);
        logger.info("generating ratios.html");
        // velocityReport("ratios.tmpl", "ratios.html", context);
        context.put("sectors", sectors);
        logger.info("generating sector.html");
        velocityReport("sector.tmpl", "sector.html", context);
    }

    private void velocityReport(String transform, String filename, VelocityContext context) throws Exception {
        // --- VELOCITY SETUP ---
        Template template = null;
        template = ve.getTemplate(transform);
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(sw.getBuffer().toString().getBytes());
        Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setIndentContent(false);
        tidy.parse(in, out);
        FileUtils.save(out.toString(), new File(reportsPath, filename));
        // FileUtils.save(sw.toString(), new File(outputPath, filename));
    }

    private void printDebug() {
        // System.out.println();
        // System.out.println("-----------");
        // System.out.println("---TOTAL---");
        // for (Ratio ratio : Ratio.values()) {
        // System.out.print(ratio.getStatistics().getStandardDeviation());
        // }
        System.out.println();
        System.out.println("-------------");
        System.out.println("---SECTORS---");
        for (Group sector : sectors.values()) {
            System.out.println(sector.getName() + ":" + sector.getCompanies().size());
            for (Company company : sector.getCompanies()) {
                String size = "";
                try {
                    size = Integer.valueOf(company.getEpic().getRatios().size()).toString();
                } catch (Exception e) {
                }
                System.out.print(company.name + " (" + size + "),");
            }
            System.out.println();
            System.out.println("---------------------------");
            // for (Ratio ratio : Ratio.values()) {
            // System.out.print(sector.getStatistics().get(ratio).getStandardDeviation());
            // }
        }
        // System.out.println();
        // System.out.println("-----------------");
        // System.out.println("---SUB SECTORS---");
        // for (Group subsector : subSectors.values()) {
        // System.out.println(subsector.getName() + ":" + subsector.getCompanies().size());
        // for (Company company : subsector.getCompanies()) {
        // System.out.print(company.name + ",");
        // }
        // System.out.println();
        // System.out.println("---------------------------");
        // for (Ratio ratio : Ratio.values()) {
        // System.out.print(subsector.getStatistics().get(ratio).getStandardDeviation());
        // }
        // }
        // for (Group sector : sectors.values()) {
        // System.out.println(Ratio.values().length + ":" + sector.getName());
        // for (Company company : sector.getCompanies()) {
        // EPIC epic = epics.get(company.getEpic());
        // if (epic != null) {
        // System.out.print("epic: " + company.getEpic() + " [");
        // for (Ratio ratio : Ratio.values()) {
        // Float value = epic.getRatios().get(ratio.toString());
        // if (value != null) {
        // String color = sector.getStatistics().get(ratio).getRatingColor(value);
        // System.out.print(ratio.toString() + ":" + value + "," + color + " - ");
        // }
        // }
        // System.out.println("]");
        // }
        // }
        // }
        // for (EPIC epic : epics.values()) {
        // System.out.println(epic.toString());
        // }
    }

    public static DateFormat getDateFormat() {
        return dateFormat;
    }
}
