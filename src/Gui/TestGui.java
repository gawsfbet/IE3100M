/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gui;

import Logic.BinStatsCalculator;
import Model.Stats.BinStats;
import Model.Stats.BoxArrangement;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Kevin-Notebook
 */
public class TestGui extends JPanel {
    
    private Rectangle binRect;
    private ArrayList<Rectangle> boxRects;
    private Rectangle[] bufferRects = new Rectangle[4];
    
    public TestGui() {
        super();
        this.binRect = new Rectangle(0, 0, 0, 0);
        this.boxRects = new ArrayList<>();
        
        this.bufferRects[0] = new Rectangle(0, 0, 0, 0);
        this.bufferRects[1] = new Rectangle(0, 0, 0, 0);
        this.bufferRects[2] = new Rectangle(0, 0, 0, 0);
        this.bufferRects[3] = new Rectangle(0, 0, 0, 0);

        //Set default close operation for JFrame

        //Set JFrame size
        //setSize(1000, 700);

        //Make JFrame visible
        setVisible(true);
    }
    
    public void setNewBinStats(BinStats binStats) {
        if (binStats == null) {
            this.binRect = new Rectangle(0, 0, 0, 0);
            this.boxRects = new ArrayList<>();

            this.bufferRects[0] = new Rectangle(0, 0, 0, 0);
            this.bufferRects[1] = new Rectangle(0, 0, 0, 0);
            this.bufferRects[2] = new Rectangle(0, 0, 0, 0);
            this.bufferRects[3] = new Rectangle(0, 0, 0, 0);
        } else {
            this.binRect = new Rectangle(0, 0, binStats.getBin().getLength(), binStats.getBin().getWidth());
            this.boxRects = new ArrayList<>();

            for (BoxArrangement arrangement : binStats.getArrangementForOneLayer()) {
                if (arrangement.isHorizontal()) {
                    boxRects.add(new Rectangle(arrangement.getX(), arrangement.getY(), binStats.getBox().getLength(), binStats.getBox().getWidth()));
                } else {
                    boxRects.add(new Rectangle(arrangement.getX(), arrangement.getY(), binStats.getBox().getWidth(), binStats.getBox().getLength()));
                }
            }

            int buffer = BinStatsCalculator.getBuffer();

            if (BinStatsCalculator.getBufferBothSides()) {
                this.bufferRects[0] = new Rectangle(0, 0, binStats.getBin().getLength() - buffer, buffer);
                this.bufferRects[3] = new Rectangle(0, buffer, buffer, binStats.getBin().getWidth() - buffer);
            } else {
                this.bufferRects[0] = new Rectangle(0, 0, 0, 0);
                this.bufferRects[3] = new Rectangle(0, 0, 0, 0);
            }

            this.bufferRects[1] = new Rectangle(binStats.getBin().getLength() - buffer, 0, buffer, binStats.getBin().getWidth() - buffer);
            this.bufferRects[2] = new Rectangle(buffer, binStats.getBin().getWidth() - buffer, binStats.getBin().getLength() - buffer, buffer);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        double scale = 1;
        
        if (binRect.getWidth() > this.getWidth() || binRect.getHeight() > this.getHeight()) {
            scale = 0.99 * Math.min((double) this.getWidth() / binRect.getWidth(), (double) this.getHeight() / binRect.getHeight());
        }
        
        g2.scale(scale, scale);
        
        drawBuffer(g2, bufferRects[0]);
        drawBuffer(g2, bufferRects[1]);
        drawBuffer(g2, bufferRects[2]);
        drawBuffer(g2, bufferRects[3]);
        
        for (Rectangle rect : boxRects) {
            drawRect(g2, rect);
        }
        
        drawBin(g2, binRect);
        
        
    }
    
    private void drawBin(Graphics2D g2, Rectangle rect) {
        BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,0,new float[] {5,5}, 0);
        g2.setStroke(dashed);
        g2.setColor(Color.BLACK);
        g2.drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
    
    private void drawRect(Graphics g, Rectangle rect) {
        g.setColor(Color.BLACK);
        g.drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        g.drawRect(rect.getX() + 1, rect.getY() + 1, rect.getWidth() - 2, rect.getHeight() - 2);
        g.setColor(Color.GREEN);
        g.fillRect(rect.getX() + 2, rect.getY() + 2, rect.getWidth() - 3, rect.getHeight() - 3);
    }
    
    private void drawBuffer(Graphics g, Rectangle rect) {
        g.setColor(Color.YELLOW);
        g.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
