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
import org.w3c.tidy.Tidy;

/**
 * http://www.londonstockexchange.com/statistics/companies-and-issuers/companies-and-issuers.htm http://www.reuters.com/finance/stocks/ratios
 * 
 * @author the.james.burton
 */
public class Analyse {
    private static Logger                   logger             = Logger.getLogger(Analyse.class.getName());
    private static final String             outputPath         = "c:/dev/first-harmonic/results/";
    private static final String             proxy              = "";
    private static final String             port               = "";
    private static final String             companyURL         = "http://www.londonstockexchange.com/statistics/companies-and-issuers/list-of-all-companies.xls";
    private static final String             securityURL        = "http://www.londonstockexchange.com/statistics/companies-and-issuers/list-of-all-uk-companies-ex-debt.xls";
    private static final String             lseDir             = outputPath + "lse";
    private static final String             companyXLS         = lseDir + "/companies.xls";
    private static final String             securityXLS        = lseDir + "/securities.xls";
    private static final String             companyFile        = lseDir + "/companies.txt";
    private static final String             securityFile       = lseDir + "/securities.txt";
    private static final String             companyFileMarker  = "List Date";
    private static final String             securityFileMarker = "List Date";
    private static final String             ratiosDir          = outputPath + "/ratios";
    private static final String             reportsPath        = outputPath + "/reports";
    private static final String             templatePath       = "c:/dev/first-harmonic/templates";
    private static final String             ratioLink          = "http://www.reuters.com/finance/stocks/ratios?symbol=";
    private static final String             ratioChartLink     = "http://www.reuters.com/charts/cr/?display=mountain&width=320&height=240&frequency=1week&duration=1year&symbol=";
    private static Queue<String>            rics               = new ConcurrentLinkedQueue<String>();
    private static BlockingQueue<HTML>      downloads          = new LinkedBlockingQueue<HTML>();
    private static Map<String, Company>     companies          = new ConcurrentHashMap<String, Company>();
    private static Map<String, Security>    securities         = new ConcurrentHashMap<String, Security>();
    private static Map<String, String>      epicName           = new ConcurrentHashMap<String, String>();
    private static Map<String, EPIC>        epics              = new ConcurrentHashMap<String, EPIC>();
    private static SortedMap<String, Group> sectors            = new TreeMap<String, Group>();
    private static SortedMap<String, Group> subSectors         = new TreeMap<String, Group>();
    private static VelocityEngine           ve;
    private static DateFormat               dateFormat         = new SimpleDateFormat("dd/MM/yyyy");

    public static void main(String args[]) throws Exception {
        System.setProperty("http.proxyHost", proxy);
        System.setProperty("http.proxyPort", port);
        new File(lseDir).mkdirs();
        new File(ratiosDir).mkdirs();
        new File(reportsPath).mkdirs();
        downloadXLS();
        createRatioHeaders();
        importCompanies(companyFile);
        importSecurities(securityFile);
        createGroup(sectors, new CompanySectorComparator(), new SectorInspector());
        createGroup(subSectors, new CompanySubSectorComparator(), new SubSectorInspector());
        importRatios(ratiosDir);
        computeStatistics();
        sortCompaniesByGroup(sectors, new CompanyMarketCapComparator());
        sortCompaniesByGroup(sectors, new CompanyRankingComparator());
        runReports();
        // printDebug();
    }

    private static void downloadXLS() throws Exception {
        logger.info("importing XLS");
        Downloader.downloadFile(companyURL, companyXLS);
        Downloader.downloadFile(securityURL, securityXLS);
        parseCompanies(companyXLS, companyFile, companyFileMarker);
        parseCompanies(securityXLS, securityFile, securityFileMarker);
    }

    private static void parseCompanies(String input, String output, String marker) throws Exception {
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

    private static boolean isRowStart(Row row, String marker) {
        boolean result = false;
        Cell cell = row.getCell(0);
        if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING && marker.equals(cell.getStringCellValue())) {
            result = true;
        }
        return result;
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

    private static void createRatioHeaders() throws Exception {
        logger.info("creating rotated ratio title headers");
        File images = new File(reportsPath, "images");
        FileUtils.deleteContents(images);
        images.mkdirs();
        for (Ratio ratio : Ratio.values()) {
            ImageUtils.rotate(ratio.getName(), new File(images, ratio.toString() + ".png"));
        }
    }

    private static void createGroup(SortedMap<String, Group> group, Comparator<Company> comparator, Inspector inspector) {
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

    private static void importRatios(String dir) {
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

    private static void computeStatistics() {
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

    private static void populateGroupStatistics(SortedMap<String, Group> group) {
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

    private static void sortCompaniesByGroup(SortedMap<String, Group> group, Comparator comparator) {
        for (Group grouping : group.values()) {
            grouping.setCompanyRankings();
            grouping.sortCompanies(comparator);
        }
    }

    private static void runReports() throws Exception {
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

    private static void velocityReport(String transform, String filename, VelocityContext context) throws Exception {
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

    private static void printDebug() {
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
