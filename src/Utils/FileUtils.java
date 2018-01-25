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
import java.util.ArrayList;

/**
 *
 * @author Kevin-Notebook
 */
public class FileUtils {
    public static ArrayList<Level3_Bin> getBinTypesFromFile() {
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<Level3_Bin> bins = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("boxes.csv"))) {
            while ((line = br.readLine()) != null) {
                // csv is a row
                String[] binData = line.split(cvsSplitBy);
                bins.add(new Level3_Bin(binData[0], 
                        Integer.parseInt(binData[1]), 
                        Integer.parseInt(binData[2]), 
                        Integer.parseInt(binData[3])));
            }
            
            return bins;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
