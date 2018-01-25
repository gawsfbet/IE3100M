/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Model.Product.Level2_Box;

/**
 *
 * @author Kevin-Notebook
 */
public class Order {
    private final Level2_Box box;
    private final int quantity;
    
    public Order(Level2_Box box, int quantity) {
        this.box = box;
        this.quantity = quantity;
    }
    
    public Level2_Box getBox() {
        return this.box;
    }
    
    public int getQuantity() {
        return this.quantity;
    }
}
