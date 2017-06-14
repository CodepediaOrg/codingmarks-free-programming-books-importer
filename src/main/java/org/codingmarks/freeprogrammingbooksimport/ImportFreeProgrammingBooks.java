package org.codingmarks.freeprogrammingbooksimport;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ama on 13.06.17.
 */
public class ImportFreeProgrammingBooks {

    public static void main(String[] args) throws IOException {

        Properties configProperties = getConfigProperties();

        MongoClientURI connectionString = new MongoClientURI(configProperties.getProperty("mongodb.connectionString"));
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("codingpedia-bookmarks");

        MongoCollection<Document> collection = database.getCollection("bookmarks");

        System.out.println("number of bookmarks :" + collection.count());
    }


    private static Properties getConfigProperties() throws IOException {
        String environment = System.getProperty("environment");
        Properties configProperties = new Properties();
        String configPropertiesFileName = "config/" + environment + ".properties";
        //InputStream inputStream = ImportFreeProgrammingBooks.class.getClassLoader().getResourceAsStream(configPropertiesFileName);
        InputStream inputStream = ImportFreeProgrammingBooks.class.getClassLoader().getResourceAsStream(configPropertiesFileName);

        configProperties.load(inputStream);
        return configProperties;
    }
}
