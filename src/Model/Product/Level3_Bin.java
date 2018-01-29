/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Product;

import java.text.DecimalFormat;

/**
 *
 * @author Kevin-Notebook
 */
public class Level3_Bin {
    private final int length;
    private final int width;
    private final int height;
    private final String name;

    public Level3_Bin(int length, int width, int height) {
        this.name = "";
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public Level3_Bin(String name, int length, int width, int height) {
        this.name = name;
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public int getLength() {
        return this.length;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getBaseArea() {
        return this.length * this.width;
    }

    public int getVolume() {
        return this.length * this.width * this.height;
    }

    public String getName() {
        return this.name;
    }

    public String getFullName() {
        return String.format("(DW) CARTON %s", this.name);
    }

    public String getDimensions() {
        DecimalFormat decFormat = new DecimalFormat("#.#");
        return String.format("%s X %s X %s cm",
                decFormat.format((float) this.length / 10),
                decFormat.format((float) this.width / 10),
                decFormat.format((float) this.height / 10));
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.getFullName(), this.getDimensions());
    }
}
