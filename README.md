This project imports [https://github.com/EbookFoundation/free-programming-books](https://github.com/EbookFoundation/free-programming-books)
 into [codingmarks.org](http://codingmarks.org)

## How to run the project
```
$ mvn clean package
```

A `.jar` **with dependencies** file will be generated that we need to execute. For example for **dev**
```
$ java -jar -Denvironment=dev free-programming-books-importer-1.0.0-SNAPSHOT-jar-with-dependencies.jar 
```
and the following for **prod**:
```
$ java -jar -Denvironment=dev free-programming-books-importer-1.0.0-SNAPSHOT-jar-with-dependencies.jar 
```

