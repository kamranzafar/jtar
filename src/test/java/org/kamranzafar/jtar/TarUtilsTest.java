package org.kamranzafar.jtar;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Author: Phillip Johnson
 * Date: 5/6/14
 */
public class TarUtilsTest {

    @Test
    public void allFilesAreLoadedIntoCollection() throws IOException{
        List<TarEntry> entries = TarUtils.getAllEntriesFromTar(new File("src/test/resources/tartest.tar"));
        assertTrue(entries.size()==6);
        List<String> fileNames = new ArrayList<>();
        for(TarEntry entry : entries){
            fileNames.add(entry.getName());
        }
        //Verify we saved each of the distinct entries
        assertTrue(fileNames.contains("tartest/one"));
        assertTrue(fileNames.contains("tartest/two"));
        assertTrue(fileNames.contains("tartest/three"));
        assertTrue(fileNames.contains("tartest/four"));
        assertTrue(fileNames.contains("tartest/five"));
        assertTrue(fileNames.contains("tartest/six"));
    }
}
