/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import java.util.Arrays;

/**
 *
 * @author Kevin-Notebook
 */
public class Test {
    
    public static void main(String[] args) {
        int[] a = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] b = Arrays.stream(a).map(x -> x * -1).toArray();
        
        for (int c : b) {
            System.out.println(c);
        }
    }
}
