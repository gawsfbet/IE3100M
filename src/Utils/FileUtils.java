/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Model.Product.Level3_Bin;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author Kevin-Notebook
 */
public class FileUtils {
    /**
     * Reads the CSV file with the given name and returns the list of Level3_Bin objects
     * @param fileName name of the file
     * @return the array of Level3_Bin objects
     * @throws IOException 
     */
    public static ArrayList<Level3_Bin> loadBinTypes(String fileName) throws IOException {
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<Level3_Bin> bins = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(FileUtils.class.getClassLoader().getResourceAsStream(fileName)));
        while ((line = br.readLine()) != null) {
            // csv is a row
            String[] binData = line.split(cvsSplitBy);
            bins.add(new Level3_Bin(binData[0], 
                    Integer.parseInt(binData[1]), 
                    Integer.parseInt(binData[2]), 
                    Integer.parseInt(binData[3])));
        }
            
        return bins;
    }
}
