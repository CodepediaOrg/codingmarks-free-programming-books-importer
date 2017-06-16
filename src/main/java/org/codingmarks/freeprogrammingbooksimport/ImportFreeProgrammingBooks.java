package org.codingmarks.freeprogrammingbooksimport;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sun.misc.Launcher;

import javax.sound.midi.SysexMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by ama on 13.06.17.
 */
public class ImportFreeProgrammingBooks {

    public static void main(String[] args) throws IOException {

        Launcher launcher = new Launcher();

        Properties configProperties = getConfigProperties();

        MongoClientURI connectionString = new MongoClientURI(configProperties.getProperty("mongodb.connectionString"));
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("codingpedia-bookmarks");

        MongoCollection<Document> bookmarksCollection = database.getCollection("bookmarks");

        System.out.println("number of bookmarks :" + bookmarksCollection.count());


        //read file line by line
        InputStream freeProgrammingBooksStream = ImportFreeProgrammingBooks.class.getClassLoader().getResourceAsStream("free-programming-books-test.md");
        BufferedReader br = new BufferedReader(new InputStreamReader(freeProgrammingBooksStream, "UTF-8"));

        String line;
        String category = null;
        String subCategory = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if(line.trim().isEmpty()){
                continue;
            } else {
                if(line.trim().startsWith("####")){
                    subCategory = getSubCategory(line);
                    System.out.println("subCategory : " + subCategory);
                } else if(line.trim().startsWith("###")) {
                    subCategory = null;
                    category = getCategory(line);
                    System.out.println("category : " + category);
                } else if(line.contains("[")){

                    Document document = new Document();

                    String title = line.substring(line.indexOf("[")+1,line.indexOf("]"));
                    System.out.println("title : " + title);
                    document.append("name", title);

                    String location = line.substring(line.indexOf("(")+1,line.indexOf(")"));
                    System.out.println("url : " + location);
                    document.append("location", location);

                    List<String> tags = new ArrayList<>();
                    tags.add("free-programming-books");//standard tag for all books
                    tags.add(category);//all links have at least one category (fall under an ### element)
                    if(subCategory != null) {
                        tags.add(subCategory);
                    }
                    System.out.println("tags : " + tags);
                    document.append("tags", tags);

                    //build description
                    String description = line.substring(line.indexOf(")") + 1, line.length());
                    if(!description.trim().isEmpty()){
                        description = description.trim();
                        if(description.startsWith("-")){
                            description = description.substring(1, description.length()).trim();
                        }
                        System.out.println("description : " + description);
                        document.append("description", description);

                        String descriptionHTML = "<p>" + description + "</p>";
                        System.out.println("descriptionHTML : " + descriptionHTML);
                        document.append("descriptionHtml", descriptionHTML);
                    }

                    //add createdAt
                    Date now = new Date();
                    System.out.println("createdAt: " + now);
                    document.append("createdAt", now);

                    //shared
                    System.out.println("shared:" + true);
                    document.append("shared", true);

                    //userId
                    System.out.println("userId:" + configProperties.getProperty("userId"));
                    document.append("userId", configProperties.getProperty("userId"));


                    //Add the document to mongoDB
                    Document doc = new Document("name", title)
                            .append("location", location)
                            .append("description", 1)
                            .append("info", new Document("x", 203).append("y", 102));

                    //verify if the document is present
                    Document bookmark = bookmarksCollection.find(eq("location", location)).first();
                    if(bookmark!=null){
                        System.out.println("*********************** Bookmark already present *********************** ");
                        System.out.println(bookmark.toJson());
                    } else {
                        bookmarksCollection.insertOne(document);
                        System.out.println("------------------- Bookmark successfuly inserted ------------------- ");
                    }

                    System.out.println("\n ");
                } else {
                    System.out.println("*********************** misc LINE : *********************** ");
                }
            }
        }

    }


    private static Properties getConfigProperties() throws IOException {
        String environment = System.getProperty("environment");
        Properties configProperties = new Properties();
        String configPropertiesFileName = "config/" + environment + ".properties";
        InputStream inputStream = ImportFreeProgrammingBooks.class.getClassLoader().getResourceAsStream(configPropertiesFileName);

        configProperties.load(inputStream);
        return configProperties;
    }

    private static String getSubCategory(String line){
        String subCategory = line.trim().substring(5, line.trim().length()).toLowerCase();
        subCategory = subCategory.replace(" ", "-"); //replace space with dashes

        return subCategory;
    }

    private static String getCategory(String line){
        String category = line.trim().substring(4, line.trim().length()).toLowerCase();
        category = category.replace(" ", "-"); //replace space with dashes

        return category;
    }


}
