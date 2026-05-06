import com.google.gson.Gson;
import org.jsoup.Jsoup;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;

import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
class Organization {
    String name;
    String address;
    String homepage;
    ArrayList<String> emails;
    ArrayList<String> phones;
    ArrayList<String> themes;
    String description;

    public Organization(String name, String address, String homepage,
                        ArrayList<String> emails, ArrayList<String> phones,
                        ArrayList<String> themes, String description) {
        this.name = name;
        this.address = address;
        this.homepage = homepage;
        this.emails = emails;
        this.phones = phones;
        this.themes = themes;
        this.description = description;
    }
}

public class WebScrapper {
    public static ArrayList<String> getEmails(Elements links) {
        ArrayList<String> emails = new ArrayList<>();
        for (Element link1 : links) {
            System.out.println(link1.attr("href"));

            if (link1.attr("href").contains("mailto:")) {
                String email = link1.attr("href").replace("mailto:", "").trim();
                if (email.isEmpty()) continue;
                emails.add(email);


            }

        }
        return emails;
    }

    public static ArrayList<String> getThemes(Element orgBlock) {
        ArrayList<String> themes = new ArrayList<>();

        Element themeElement = orgBlock.selectFirst(
        "p:has(b:contains(Themenbereich))"
        );

        if (themeElement == null) {
            return themes;
        }

        String html = themeElement.html();

        html = html.replaceFirst("(?s)<b>.*?</b>", "");

        String[] parts = html.split("<br\\s*/?>");

        for (String part : parts) {
            String clean = Jsoup.parse(part).text().trim();

            if (!clean.isEmpty()) {
                themes.add(clean);
            }
        }

        return themes;
    }


    public static String getHomepage(Element orgBlock) {
        Element leftColumn = orgBlock.selectFirst(".col-md-5");
        if (leftColumn == null) {
            return "The template of the website has changed";
        }
        Element homepage = leftColumn.selectFirst("p:has(b:contains(Homepage))");
        if (homepage == null) {
            return "not found";
        }
        Element link = homepage.selectFirst("a[href]");


        if (link != null) {

            return link.attr("href").trim();

        }
        String trim = homepage.ownText().trim();
        if (trim.isEmpty()){
            return "not found";
        }
        return trim;

    }






    public static String getAddress(Element orgBlock){
        Element leftColumn = orgBlock.selectFirst(".col-md-5");
        Element nameElement = leftColumn.selectFirst("p:has(b:contains(Adresse))");
        if(nameElement==null){
            return "not found";
        }
        return nameElement.ownText().trim();

    }
    public static String getName(Element orgBlock){
        Element leftColumn = orgBlock.selectFirst(".col-md-5");
        Element nameElement = leftColumn.selectFirst("p:has(b:contains(Name))");
        if (nameElement==null) return "Name not found";
        return nameElement.ownText().trim();

    }

    public static String getDescription(Elements paragraphs) {
        for (Element paragraph : paragraphs) {
            if (paragraph.text().contains("Ziel der Organisation:")) {
                return paragraph.text().replace("Ziel der Organisation:", "").trim();
            }
        }
        return "No description found";
    }
    public static ArrayList<String> getNumbers(Element orgBlock){
        ArrayList<String> numbers = new ArrayList<>();
        Elements selected=orgBlock.select("p:has(b:contains(Telefon))");
        for (Element element : selected) {

            String trim = element.ownText().trim();
            if (trim.isEmpty()) continue;
            numbers.add(trim);
        }
        return numbers;

    }
    public static void main(String[] args) throws IOException {
        String folderPath="/Users/flaviusvacarita/Documents/WebSearchAndMining";
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            System.out.println("Folder not found");
            return;
        }
        ArrayList<Organization> allOrgs = new ArrayList<>();

        for (File file : listOfFiles) {   //iteration over the HTML files
            if(!file.getName().endsWith(".html")){continue;}
            System.out.println("File: " + file.getName());
            Document doc = Jsoup.parse(file, "UTF-8");
            Element orgBlock=doc.selectFirst(".organisationsingle");
            if(orgBlock==null){
                System.out.println("No organisation block found");
                System.out.println(file.getAbsolutePath());
                continue;
            }
            Elements link= orgBlock.getElementsByTag("a");
            Elements paragraphs= orgBlock.getElementsByTag("p");
            for(var paragraph : paragraphs){
                System.out.println("Printing this now");
                System.out.println(paragraph.text());
            }
            String name = getName(orgBlock);
            String address = getAddress(orgBlock);
            String homepage = getHomepage(orgBlock);
            ArrayList<String> emails = getEmails(link);
            ArrayList<String> phones = getNumbers(orgBlock);
            ArrayList<String> themes = getThemes(orgBlock);
            String description = getDescription(paragraphs);

            Organization org = new Organization(
            name, address, homepage,
            emails, phones, themes, description
            );

            allOrgs.add(org);




            }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        FileWriter writer = new FileWriter("organizations.json");

        gson.toJson(allOrgs, writer);

        writer.close();














        }}







