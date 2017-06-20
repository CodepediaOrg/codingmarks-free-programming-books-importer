package org.codingmarks.freeprogrammingbooksimport;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;

/**
 * Created by ama on 13.06.17.
 */
public class FreeProgrammingBooksImporter {

    public static void main(String[] args) throws IOException {

        Properties configProperties = getConfigProperties();

        MongoClientURI connectionString = new MongoClientURI(configProperties.getProperty("mongodb.connectionString"));
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("codingpedia-bookmarks");

        MongoCollection<Document> bookmarksCollection = database.getCollection("bookmarks");

        System.out.println("number of bookmarks :" + bookmarksCollection.count());

        File dir = new File(args[0]);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith("free-programming-books") && name.endsWith(".md"));
        int numberOfInsertedBookmarks = 0;
        for(int i=0; i < files.length; i++ ){
           String fileName =  files[i].getName();
           System.out.println("-------------- Reading File : " + fileName + " --------------");

            //read file line by line
/*            try (InputStream freeProgrammingBooksStream =
                         FreeProgrammingBooksImporter.class.getClassLoader().getResourceAsStream(args[0])){*/
            try (InputStream freeProgrammingBooksStream =
                         new FileInputStream(files[i])) {

                try(BufferedReader br =
                            new BufferedReader(new InputStreamReader(freeProgrammingBooksStream, "UTF-8"))){
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
                            } else if(line.contains("[") && !category.equals("index")){ //we skip the initial index

                                Document document = new Document();

                                String title = line.substring(line.indexOf("[")+1,line.indexOf("]"));
                                System.out.println("title : " + title);
                                document.append("name", title);

                                line = line.substring(line.indexOf("]")+1, line.length());//it can happen that some titles have () in them

                                String location = line.substring(line.indexOf("(")+1,line.indexOf(")"));
                                System.out.println("url : " + location);
                                document.append("location", location);

                                List<String> tags = new ArrayList<>();
                                tags.add(getFileNameWithoutExtension(fileName));//standard tag for all books
                                tags.add(category);//all links have at least one category (fall under an ### element)
                                if(subCategory != null) {
                                    tags.add(subCategory);
                                }
                                System.out.println("tags : " + tags);
                                document.append("tags", tags);

                                //build description
                                String description = line.substring(line.indexOf(")") + 1, line.length());
                                description = description.trim();
                                if(description.startsWith("-")){
                                    description = description.substring(1, description.length()).trim();
                                }
                                System.out.println("description : " + description);
                                document.append("description", description);

                                String descriptionHTML = "<p>" + description + "</p>";
                                System.out.println("descriptionHTML : " + descriptionHTML);
                                document.append("descriptionHtml", descriptionHTML);


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

                                //get language code
                                System.out.println("language: " + getLanguageCode(fileName));
                                document.append("language", getLanguageCode(fileName));

                                //verify if the document is present
                                Document bookmark = bookmarksCollection.find(eq("location", location)).first();
                                if(bookmark!=null){
                                    System.out.println("*********************** Bookmark already present *********************** ");
                                    System.out.println(bookmark.toJson());
                                } else {
                                    bookmarksCollection.insertOne(document);
                                    numberOfInsertedBookmarks++;
                                    System.out.println("------------------- Bookmark successfuly inserted ------------------- ");
                                }

                                System.out.println("\n ");
                            } else {
                                System.out.println("*********************** misc LINE : *********************** ");
                            }
                        }
                    }
                }

            }

        }

        System.out.println("Number of inserted bookmarks : " + numberOfInsertedBookmarks);
    }

    private static Properties getConfigProperties() throws IOException {
        String environment = System.getProperty("environment");
        Properties configProperties = new Properties();
        String configPropertiesFileName = "config/" + environment + ".properties";
        InputStream inputStream = FreeProgrammingBooksImporter.class.getClassLoader().getResourceAsStream(configPropertiesFileName);

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


    public static String getFileNameWithoutExtension(String filename){
        int pos = filename.lastIndexOf(".");
        if (pos > 0) {
            filename = filename.substring(0, pos);
        }

        return filename;
    }

    public static String getLanguageCode(String filename){
        filename = getFileNameWithoutExtension(filename);
        filename = filename.substring("free-programming-books".length(), filename.length());
        //now it's left only the language code/locale in some cases
        if(!filename.isEmpty()){
            return filename.substring(1,3);//first two letters of "pt_br" is "pt"
        }

        return "en";//default is English
    }


}
