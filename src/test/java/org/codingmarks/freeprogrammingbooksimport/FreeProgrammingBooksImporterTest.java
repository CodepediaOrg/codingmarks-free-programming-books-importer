package org.codingmarks.freeprogrammingbooksimport;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ama on 20.06.17.
 */
public class FreeProgrammingBooksImporterTest {

    @Test
    public void testGetDefaultLanguageCode(){
        String filaname="free-programming-books.md";

        String languageCode = FreeProgrammingBooksImporter.getLanguageCode(filaname);

        Assert.assertTrue("en".equals(languageCode));
    }

    @Test
    public void testGetLanguageCode(){
        String filaname="free-programming-books-ro.md";

        String languageCode = FreeProgrammingBooksImporter.getLanguageCode(filaname);

        Assert.assertTrue("ro".equals(languageCode));
    }

    @Test
    public void testGetLanguageCodeWithLocale(){
        String filaname="free-programming-books-pt_BR.md";

        String languageCode = FreeProgrammingBooksImporter.getLanguageCode(filaname);

        Assert.assertTrue("pt".equals(languageCode));
    }
}
