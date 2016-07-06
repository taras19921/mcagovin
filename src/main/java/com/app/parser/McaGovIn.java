package com.app.parser;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class McaGovIn {

    public static void main(String[] args)
    {

        List<String> allCIN = getAllCIN("https://www.zaubacorp.com/company-list/p-1-company.html");
        getCompanies(allCIN);
    }

    public static List<String> getAllCIN(String link) {

        System.out.println("Success!!!");
        List<String> pages = new ArrayList<>();
        List<String> allCIN = new ArrayList<>();
        String CIN;
        String last_page = "";

        String urlDB = "jdbc:mysql://localhost:3306/companies";
        String user = "root";
        String pass = "root";
        try {

            java.sql.Connection conn = DriverManager.getConnection(urlDB, user, pass);
            Statement stmt = conn.createStatement();

            Document page = null;
            try {
                page = Jsoup.connect(link).timeout(0).parser(Parser.htmlParser()).get();
            } catch (HttpStatusException hse) {
                System.out.println("HttpStatusException with link: " + link);
                // try connect again
                getAllCIN(link);
            } catch (SocketTimeoutException ste) {
                System.out.println("SocketTimeoutException with link: " + link);
                // try connect again
                getAllCIN(link);
            } catch (SocketException se) {
                System.out.println("SocketException with link: " + link);
                // try connect again
                getAllCIN(link);
            } catch (UnknownHostException uhe) {
                System.out.println("UnknownHostException with link: " + link);
                // try connect again
                getAllCIN(link);
            }
            Elements links = page.select("a[rel=nofollow]");
            for (int i = 0; i < 10; i++)
                if (i < 6) {
                    link = links.get(i).attr("href");
                    last_page = link;
                    Document document;
                    try {
                        document = Jsoup.connect(link).timeout(0).parser(Parser.htmlParser())
//                                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                                .get();
                    } catch (HttpStatusException hse) {
                        System.out.println("HttpStatusException with link: " + link);
                        // try connect again
                        i--;
                        continue;
                    } catch (SocketTimeoutException ste) {
                        System.out.println("SocketTimeoutException with link: " + link);
                        // try connect again
                        i--;
                        continue;
                    } catch (SocketException se) {
                        System.out.println("SocketException with link: " + link);
                        // try connect again
                        i--;
                        continue;
                    } catch (UnknownHostException uhe) {
                        System.out.println("UnknownHostException with link: " + link);
                        // try connect again
                        i--;
                        continue;
                    }

                    Element table1 = document.getElementById("table").select("table").first();
                    Elements rows1 = table1.select("tbody").select("tr");

                    for (int row = 0; row < rows1.size(); row++) {
                        Element rowCIN = rows1.get(row);
                        CIN = rowCIN.select("td").get(0).text();
                        if (CIN.equals("U74999DL2007PTC161616")) {
                            System.out.println("CIN: " + CIN);
                            System.out.println("link: " + link);
                        }
                        if (CIN.length() > 10) {
                            String selectByCIN = "SELECT * FROM `cin` WHERE `cin` LIKE '" + CIN + "'";
                            ResultSet rs = stmt.executeQuery(selectByCIN);
                            if (!rs.next())
                                allCIN.add(CIN);
                        } else if (CIN.startsWith("F")) {
                            String selectByFRCN = "SELECT * FROM `fcrn` WHERE `fcrn` LIKE '" + CIN + "'";
                            ResultSet rs = stmt.executeQuery(selectByFRCN);
                            if (!rs.next())
                                allCIN.add(CIN);
                        } else {
                            String selectByLLPIN = "SELECT * FROM `llp` WHERE `llpin` LIKE '" + CIN + "'";
                            ResultSet rs = stmt.executeQuery(selectByLLPIN);
                            if (!rs.next())
                                allCIN.add(CIN);
                        }
                    }
                    pages.add(link);
                } else break;
            while (true) {
                if (pages.size() % 10 == 0) {
                    System.out.println("pages.size(): " + pages.size());
                    System.out.println("last_page: " + pages.get(pages.size() - 1));
                    return allCIN;
                }
                link = pages.get(pages.size() - 1);
                try {
                    page = Jsoup.connect(link).timeout(0).parser(Parser.htmlParser()).get();
                } catch (HttpStatusException hse) {
                    System.out.println("HttpStatusException with link: " + link);
                    // try connect again
                    continue;
                } catch (SocketTimeoutException ste) {
                    System.out.println("SocketTimeoutException with link: " + link);
                    // try connect again
                    continue;
                } catch (SocketException se) {
                    System.out.println("SocketException with link: " + link);
                    // try connect again
                    continue;
                } catch (UnknownHostException uhe) {
                    System.out.println("UnknownHostException with link: " + link);
                    // try connect again
                    continue;
                }
                links = page.select("a[rel=nofollow]");

                String nextLink = links.get(5).attr("href");
                String lastLink = pages.get(pages.size() - 1);

                // if 5 != 6
                if (!nextLink.equals(lastLink)) {
                    pages.add(nextLink);
                    last_page = nextLink;

                    Document document;
                    try {
                        document = Jsoup.connect(nextLink).timeout(0).parser(Parser.htmlParser()).get();
                    } catch (HttpStatusException hse) {
                        System.out.println("HttpStatusException with link: " + link);
                        // try connect again
                        pages.remove(pages.size() - 1);
                        continue;
                    } catch (SocketTimeoutException ste) {
                        System.out.println("SocketTimeoutException with link: " + link);
                        // try connect again
                        pages.remove(pages.size() - 1);
                        continue;
                    } catch (SocketException se) {
                        System.out.println("SocketException with link: " + link);
                        // try connect again
                        pages.remove(pages.size() - 1);
                        continue;
                    } catch (UnknownHostException uhe) {
                        System.out.println("UnknownHostException with link: " + link);
                        // try connect again
                        pages.remove(pages.size() - 1);
                        continue;
                    }
                    Element table1 = document.getElementById("table").select("table").first();
                    Elements rows1 = table1.select("tbody").select("tr");

                    for (int row = 0; row < rows1.size(); row++) {
                        Element rowCIN = rows1.get(row);
                        CIN = rowCIN.select("td").get(0).text();
                        if (CIN.equals("F03159")) {
                            System.out.println("CIN: " + CIN);
                            System.out.println("link: " + link);
                        }
                        if (CIN.length() > 10) {
                            String selectByCIN = "SELECT * FROM `cin` WHERE `cin` LIKE '" + CIN + "'";
                            ResultSet rs = stmt.executeQuery(selectByCIN);
                            if (!rs.next())
                                allCIN.add(CIN);
                        } else if (CIN.startsWith("F")) {
                            String selectByFRCN = "SELECT * FROM `fcrn` WHERE `fcrn` LIKE '" + CIN + "'";
                            ResultSet rs = stmt.executeQuery(selectByFRCN);
                            if (!rs.next())
                                allCIN.add(CIN);
                        } else {
                            String selectByLLPIN = "SELECT * FROM `llp` WHERE `llpin` LIKE '" + CIN + "'";
                            ResultSet rs = stmt.executeQuery(selectByLLPIN);
                            if (!rs.next())
                                allCIN.add(CIN);
                        }
                    }
                } else {
                    for (int i = 6; i < links.size() - 1; i++) {

                        link = links.get(i).attr("href");
                        Document document;
                        try {
                            document = Jsoup.connect(link).timeout(0/*10 * 1000*/).parser(Parser.htmlParser()).get();
                        } catch (HttpStatusException hse) {
                            System.out.println("HttpStatusException with link: " + link);
                            // try connect again
                            i--;
                            continue;
                        } catch (SocketTimeoutException ste) {
                            System.out.println("SocketTimeoutException with link: " + link);
                            // try connect again
                            i--;
                            continue;
                        } catch (SocketException se) {
                            System.out.println("SocketException with link: " + link);
                            // try connect again
                            i--;
                            continue;
                        } catch (UnknownHostException uhe) {
                            System.out.println("UnknownHostException with link: " + link);
                            // try connect again
                            i--;
                            continue;
                        }

                        Element table1 = document.getElementById("table").select("table").first();
                        Elements rows1 = table1.select("tbody").select("tr");

                        for (int row = 0; row < rows1.size(); row++) {
                            Element rowCIN = rows1.get(row);
                            CIN = rowCIN.select("td").get(0).text();
                            if (CIN.equals("F03159")) {
                                System.out.println("CIN: " + CIN);
                                System.out.println("link: " + link);
                            }
                            if (CIN.length() > 10) {
                                String selectByCIN = "SELECT * FROM `cin` WHERE `cin` LIKE '" + CIN + "'";
                                ResultSet rs = stmt.executeQuery(selectByCIN);
                                if (!rs.next())
                                    allCIN.add(CIN);
                            } else if (CIN.startsWith("F")) {
                                String selectByFRCN = "SELECT * FROM `fcrn` WHERE `fcrn` LIKE '" + CIN + "'";
                                ResultSet rs = stmt.executeQuery(selectByFRCN);
                                if (!rs.next())
                                    allCIN.add(CIN);
                            } else {
                                String selectByLLPIN = "SELECT * FROM `llp` WHERE `llpin` LIKE '" + CIN + "'";
                                ResultSet rs = stmt.executeQuery(selectByLLPIN);
                                if (!rs.next())
                                    allCIN.add(CIN);
                            }
                        }
                        pages.add(link);
                    }
                    return allCIN;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            System.out.println("count of companies: " + allCIN.size());
            System.out.println("last_page: " + last_page);
            return allCIN;
        }
    }

    public static void getCompanies(List<String> allCIN) {
        try {
            String urlDB = "jdbc:mysql://localhost:3306/companies";
            String user = "root";
            String pass = "root";
            java.sql.Connection conn = DriverManager.getConnection(urlDB, user, pass);
            Statement stmt = conn.createStatement();

            String url = "http://www.mca.gov.in/mcafoportal/companyLLPMasterData.do";
            Connection.Response response;

            String NumberOfPartners = "";
            String NumberOfDesignatedPartners = "";
            String PreviousFirm = "";
            String TotalObligation = "";
            String MainDivision = "";
            String DescriptionOfMainDivision = "";
            String DateOffLastFinancialYearOfAccounts = "";
            String DateOffLastFinancialYearOfAnnual = "";
            String CompanyName = "";
            String ROCCode = "";
            String RegistrationNumber = "";
            String CompanyCategory = "";
            String CompanySubCategory = "";
            String ClassOfCompany = "";
            String AuthorisedCapital = "";
            String PaidUpCapital = "";
            String NumberOfMembers = "";
            String DateOfIncorporation = "";
            String CountryOfIncorporation = "";
            String RegisteredAddress = "";
            String EmailId = "";
            String ForeignCompanyWithShareCapital = "";
            String TypeOfOffice = "";
            String Details = "";
            String MainDivisionOfBusinessActivity = "";
            String WhetherListed = "";
            String DateOfLastAGM = "";
            String DateOfBalanceSheet = "";
            String CompanyStatus = "";
            String AssetsUnderCharge;
            String ChargeAmount;
            String DateOfCreation;
            String DateOfModification;
            String Status;
            String DIN = "";
            String Name;
            String BeginDate;
            String EndDate;
            for (int cin = 0; cin < allCIN.size(); cin++) {

                String CIN = null;
                String LLPIN = null;
                String FCRN = null;

                try {
                    response = Jsoup.connect(url)
                        /*.timeout(10 * 1000)*/
//                        .data("companyID", cin)
                            .data("companyID", allCIN.get(cin))
//                    .data("companyID", "U92142KL1996PTC010396")
//                            .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                            .timeout(12000)
                            .method(Connection.Method.POST)
                            .execute();
                } catch (HttpStatusException hse) {
                    System.out.println("HttpStatusException with CIN: " + allCIN.get(cin));
                    // try again
                    cin--;
                    continue;
                } catch (SocketTimeoutException ste) {
                    System.out.println("SocketTimeoutException with CIN: " + allCIN.get(cin));
                    // try again
                    cin--;
                    continue;
                } catch (SocketException se) {
                    System.out.println("SocketException with CIN: " + allCIN.get(cin));
                    // try again
                    cin--;
                    continue;
                } catch (UnknownHostException uhe) {
                    System.out.println("UnknownHostException with CIN: " + allCIN.get(cin));
                    // try again
                    cin--;
                    continue;
                }

                if (response != null) {
                    Document document = response.parse();
                    Element table1;
                    Elements rows;
                    try {
                        table1 = document.getElementsByClass("result-forms").select("table").first();
                        rows = table1.select("tbody").select("tr");
                    } catch (NullPointerException npe) {
                        System.out.println("NullPointerException with CIN: " + allCIN.get(cin));
                        cin--;
                        continue;
                    }

                    if (rows.size() != 0) {
                        if (rows.get(0).select("td").get(0).text().contains("CIN")) {
                            CIN = rows.get(0).select("td").get(1).text().replaceAll("'", "");
                            for (int i = 1; i < rows.size(); i++) {
                                Element row = rows.get(i);

                                if (i == 1) {
                                    CompanyName = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 2) {
                                    ROCCode = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 3) {
                                    RegistrationNumber = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 4) {
                                    CompanyCategory = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 5) {
                                    CompanySubCategory = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 6) {
                                    ClassOfCompany = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 7) {
                                    AuthorisedCapital = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 8) {
                                    PaidUpCapital = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 9) {
                                    NumberOfMembers = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 10) {
                                    DateOfIncorporation = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 11) {
                                    RegisteredAddress = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 12) {
                                    EmailId = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 13) {
                                    WhetherListed = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 14) {
                                    DateOfLastAGM = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 15) {
                                    DateOfBalanceSheet = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 16) {
                                    CompanyStatus = row.select("td").get(1).text().replaceAll("'", "");
                                }
                            }
                            String insert1 = "INSERT INTO `cin` (`cin`, `company_name`, `roc_code`, `registration_number`, `company_category`," +
                                    " `company_sub_category`, `class_of_company`, `authorised_capital`, `paid_up_capital`, `number_of_members`," +
                                    " `date_of_incorporation`, `registered_address`, `email_id`, `whether_listed`," +
                                    " `date_of_last_agm`, `date_of_balance_sheet`, `company_status`) VALUES (";
                            insert1 += "'" + CIN + "', '" + CompanyName + "', '" + ROCCode + "', '" + RegistrationNumber + "', '" + CompanyCategory + "', '" + CompanySubCategory + "', '"
                                    + ClassOfCompany + "', '" + AuthorisedCapital + "', '" + PaidUpCapital + "', '" + NumberOfMembers + "', '" + DateOfIncorporation + "', '"
                                    + RegisteredAddress + "', '" + EmailId + "', '" + WhetherListed + "', '" + DateOfLastAGM + "', '" + DateOfBalanceSheet + "', '" + CompanyStatus + "');";
                            try {
                                stmt.executeUpdate(insert1);
                                System.out.println("CIN: " + CIN);
                            } catch (MySQLSyntaxErrorException e) {
                                System.out.println("MySQLSyntaxErrorException with CIN: " + CIN);
                                continue;
                            } catch (MySQLIntegrityConstraintViolationException e) {
                                System.out.println("MySQLSyntaxErrorException with CIN: " + CIN);
                                System.out.println("allCIN.get(cin): " + allCIN.get(cin));
                                continue;
                            }
                        } else if (rows.get(0).select("td").get(0).text().contains("LLPIN")) {
                            LLPIN = rows.get(0).select("td").get(1).text().replaceAll("'", "");
                            for (int i = 1; i < rows.size(); i++) {
                                Element row = rows.get(i);

                                if (i == 1) {
                                    CompanyName = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 2) {
                                    NumberOfPartners = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 3) {
                                    NumberOfDesignatedPartners = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 4) {
                                    ROCCode = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 5) {
                                    DateOfIncorporation = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 6) {
                                    RegisteredAddress = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 7) {
                                    EmailId = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 8) {
                                    PreviousFirm = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 9) {
                                    TotalObligation = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 10) {
                                    MainDivision = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 11) {
                                    DescriptionOfMainDivision = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 12) {
                                    DateOffLastFinancialYearOfAccounts = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 13) {
                                    DateOffLastFinancialYearOfAnnual = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 14) {
                                    CompanyStatus = row.select("td").get(1).text().replaceAll("'", "");
                                }
                            }
                            String insert1 = "INSERT INTO `llp` (`llpin`, `llp_name`, `number_of_partners`, `number_of_designated_partners`, `roc_code`, " +
                                    " `date_of_incorporation`, `registered_address`, `email_id`, `previous_firm`, `total_obligation`," +
                                    " `main_division`, `description_of_main_division`, `date_of_last_financial_year_statement_of_accounts_field`," +
                                    " `date_of_last_financial_year_annual_return_field`, `llp_status`) VALUES (";
                            insert1 += "'" + LLPIN + "', '" + CompanyName + "', '" + NumberOfPartners + "', '" + NumberOfDesignatedPartners + "', '" + ROCCode + "', '"
                                    + DateOfIncorporation + "', '" + RegisteredAddress + "', '" + EmailId + "', '" + PreviousFirm + "', '" + TotalObligation + "', '"
                                    + MainDivision + "', '" + DescriptionOfMainDivision + "', '" + DateOffLastFinancialYearOfAccounts + "', '" + DateOffLastFinancialYearOfAnnual + "', '" + CompanyStatus + "');";
                            try {
                                stmt.executeUpdate(insert1);
                                System.out.println("LLPIN: " + LLPIN);
                            } catch (MySQLSyntaxErrorException e) {
                                System.out.println("MySQLSyntaxErrorException with LLPIN: " + LLPIN);
                                continue;
                            } catch (MySQLIntegrityConstraintViolationException e) {
                                System.out.println("MySQLSyntaxErrorException with LLPIN: " + LLPIN);
                                System.out.println("allCIN.get(cin): " + allCIN.get(cin));
                                continue;
                            }
                        } else if (rows.get(0).select("td").get(0).text().contains("FCRN")) {
                            FCRN = rows.get(0).select("td").get(1).text().replaceAll("'", "");
                            for (int i = 1; i < rows.size(); i++) {
                                Element row = rows.get(i);

                                if (i == 1) {
                                    CompanyName = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 2) {
                                    DateOfIncorporation = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 3) {
                                    CountryOfIncorporation = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 4) {
                                    RegisteredAddress = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 5) {
                                    EmailId = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 6) {
                                    ForeignCompanyWithShareCapital = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 7) {
                                    CompanyStatus = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 8) {
                                    TypeOfOffice = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 9) {
                                    Details = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 10) {
                                    MainDivisionOfBusinessActivity = row.select("td").get(1).text().replaceAll("'", "");
                                } else if (i == 11) {
                                    DescriptionOfMainDivision = row.select("td").get(1).text().replaceAll("'", "");
                                }
                            }
                            String insert1 = "INSERT INTO `fcrn` (`fcrn`, `company_name`, `date_of_incorporation`, `contry_of_incorporation`, `registered_address`," +
                                    " `email_id`, `foreign_company_with_share_capital`, `company_status`, `type_of_ ofice`, `details`," +
                                    " `main_division_of_business_activity`, `description_of_main_division`) VALUES (";
                            insert1 += "'" + FCRN + "', '" + CompanyName + "', '" + DateOfIncorporation + "', '" + CountryOfIncorporation + "', '" + RegisteredAddress + "', '" + EmailId + "', '"
                                    + ForeignCompanyWithShareCapital + "', '" + CompanyStatus + "', '" + TypeOfOffice + "', '" + Details + "', '" + MainDivisionOfBusinessActivity + "', '"
                                    + DescriptionOfMainDivision + "');";
                            try {
                                stmt.executeUpdate(insert1);
                                System.out.println("FCRN: " + FCRN);
                            } catch (MySQLSyntaxErrorException e) {
                                System.out.println("MySQLSyntaxErrorException with FCRN: " + FCRN);
                                continue;
                            } catch (MySQLIntegrityConstraintViolationException e) {
                                System.out.println("MySQLIntegrityConstraintViolationException with FCRN: " + FCRN);
                                System.out.println("allCIN.get(cin): " + allCIN.get(cin));
                                continue;
                            }
                        }

                        /*if (CIN.equals("U28113MH2012PTC237442"))
                            System.out.println("bug");*/
                        Element table2 = document.getElementById("resultTab5").select("table").first();
                        Elements tr = table2.select("tr[class=table-row]");
                        if (tr.size() == 0) {
                            Elements rows2 = table2.select("tbody").select("tr");
                            for (int i = 1; i < rows2.size(); i++) {
                                Elements tds = rows2.get(i).select("td");
                                AssetsUnderCharge = tds.get(0).text().replaceAll("'", "");
                                ChargeAmount = tds.get(1).text().replaceAll("'", "");
                                DateOfCreation = tds.get(2).text().replaceAll("'", "");
                                DateOfModification = tds.get(3).text().replaceAll("'", "");
                                Status = tds.get(4).text().replaceAll("'", "");
                                String insert2 = null;
                                if (CIN != null) {
                                    insert2 = "INSERT INTO `charges` (`assets_under_charge`, `charge_amount`, `date_of_creation`, `date_of_modification`," +
                                            " `status`, `cin`) VALUES (";
                                    insert2 += "'" + AssetsUnderCharge + "', '" + ChargeAmount + "', '" + DateOfCreation + "', '" + DateOfModification + "', '" + Status + "', '" + CIN + "');";
                                } else if (LLPIN != null) {
                                    insert2 = "INSERT INTO `charges` (`assets_under_charge`, `charge_amount`, `date_of_creation`, `date_of_modification`," +
                                            " `status`, `llp`) VALUES (";
                                    insert2 += "'" + AssetsUnderCharge + "', '" + ChargeAmount + "', '" + DateOfCreation + "', '" + DateOfModification + "', '" + Status + "', '" + LLPIN + "');";
                                } else if (FCRN != null) {
                                    insert2 = "INSERT INTO `fcrn` (`assets_under_charge`, `charge_amount`, `date_of_creation`, `date_of_modification`," +
                                            " `status`, `fcrn`) VALUES (";
                                    insert2 += "'" + AssetsUnderCharge + "', '" + ChargeAmount + "', '" + DateOfCreation + "', '" + DateOfModification + "', '" + Status + "', '" + FCRN + "');";
                                }
                                stmt.executeUpdate(insert2);
                            }
                        }

                        Element table3 = document.getElementById("resultTab6").select("table").first();
                        tr = table3.select("tr[class=table-row]");
                        if (tr.size() == 0) {
                            Elements rows3 = table3.select("tbody").select("tr");
                            for (int i = 1; i < rows3.size(); i++) {
                                Elements tds = rows3.get(i).select("td");
                                Element tdDIN = tds.get(0).getElementById("tdShowDirectorMasterdata");
                                if (tdDIN != null)
                                    DIN = tdDIN.text().replaceAll("'", "");
                                Name = tds.get(1).text().replaceAll("'", "");
                                BeginDate = tds.get(2).text().replaceAll("'", "");
                                EndDate = tds.get(3).text().replaceAll("'", "");
                                String insert3 = null;
                                if (CIN != null) {
                                    insert3 = "INSERT INTO `directors` (`din`, `name`, `begin_date`, `end_date`," +
                                            " `cin`) VALUES (";
                                    insert3 += "'" + DIN + "', '" + Name + "', '" + BeginDate + "', '" + EndDate + "', '" + CIN + "');";
                                } else if (LLPIN != null) {
                                    insert3 = "INSERT INTO `directors` (`din`, `name`, `begin_date`, `end_date`," +
                                            " `llp`) VALUES (";
                                    insert3 += "'" + DIN + "', '" + Name + "', '" + BeginDate + "', '" + EndDate + "', '" + LLPIN + "');";
                                } else if (FCRN != null) {
                                    insert3 = "INSERT INTO `directors` (`din`, `name`, `begin_date`, `end_date`," +
                                            " `fcrn`) VALUES (";
                                    insert3 += "'" + DIN + "', '" + Name + "', '" + BeginDate + "', '" + EndDate + "', '" + FCRN + "');";
                                }
                                stmt.executeUpdate(insert3);
                            }
                        }
                    }
                }
            }
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
