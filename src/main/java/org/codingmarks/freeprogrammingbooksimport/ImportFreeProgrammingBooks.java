package org.codingmarks.freeprogrammingbooksimport;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sun.misc.Launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

        MongoCollection<Document> collection = database.getCollection("bookmarks");

        System.out.println("number of bookmarks :" + collection.count());


        //read file line by line
        InputStream freeProgrammingBooksStream = ImportFreeProgrammingBooks.class.getClassLoader().getResourceAsStream("free-programming-books.md");
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
                    String title = line.substring(line.indexOf("[")+1,line.indexOf("]"));
                    System.out.println("title : " + title);
                    String url = line.substring(line.indexOf("(")+1,line.indexOf(")"));
                    System.out.println("url : " + url);
                    List<String> tags = new ArrayList<>();
                    tags.add("free-programming-books");//standard tag for all books
                    tags.add(category);//all links have at least one category (fall under an ### element)
                    if(subCategory != null) {
                        tags.add(subCategory);
                    }
                    System.out.println("tags : " + tags);

                    //build description
                    String description = line.substring(line.indexOf(")") + 1, line.length());
                    if(!description.trim().isEmpty()){
                        description = description.trim();
                        if(description.startsWith("-")){
                            description = description.substring(1, description.length()).trim();
                        }
                        System.out.println("description : " + description);
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
