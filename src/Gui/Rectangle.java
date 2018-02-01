/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gui;

/**
 *
 * @author Kevin-Notebook
 */
public class Rectangle {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Rectangle(int x,int y,int width,int height)
    {
        this.x = x;
        this.y = y;
        this.width = width < 0 ? 0 : width;
        this.height = height < 0 ? 0 : height;
    }

    public Rectangle(Rectangle a)
    {
        this.x = a.x;
        this.y = a.y;
        this.width = a.width;
        this.height = a.height;
    }

    public String toString()
    {
        return "Start: ("+x+","+y+"), Width: "+width+", Height: "+height+"\n";
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
