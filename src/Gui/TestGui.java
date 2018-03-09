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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JFrame;

/**
 *
 * @author Kevin-Notebook
 */
public class TestGui extends JFrame {
    private static final int OFFSET_X = 50;
    private static final int OFFSET_Y = 50;
    
    private final Rectangle binRect;
    private final ArrayList<Rectangle> boxRects;
    private final Rectangle[] bufferRects = new Rectangle[4];
    
    public TestGui(BinStats binStats) {
        super("Recommended packing configuration");
        
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
        this.bufferRects[0] = new Rectangle(0, 0, binStats.getBin().getLength() - buffer, buffer);
        this.bufferRects[1] = new Rectangle(binStats.getBin().getLength() - buffer, 0, buffer, binStats.getBin().getWidth() - buffer);
        this.bufferRects[2] = new Rectangle(buffer, binStats.getBin().getWidth() - buffer, binStats.getBin().getLength() - buffer, buffer);
        this.bufferRects[3] = new Rectangle(0, buffer, buffer, binStats.getBin().getWidth() - buffer);

        //Set default close operation for JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set JFrame size
        setSize(1000, 700);

        //Make JFrame visible
        setVisible(true);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        
        drawBuffer(g, bufferRects[0]);
        drawBuffer(g, bufferRects[1]);
        drawBuffer(g, bufferRects[2]);
        drawBuffer(g, bufferRects[3]);
        
        for (Rectangle rect : boxRects) {
            drawRect(g, rect);
        }
        
        drawBin(g2, binRect);
    }
    
    private void drawBin(Graphics2D g2, Rectangle rect) {
        BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,0,new float[] {5,5}, 0);
        g2.setStroke(dashed);
        g2.setColor(Color.BLACK);
        g2.drawRect(OFFSET_X + rect.getX(), OFFSET_Y + rect.getY(), rect.getWidth(), rect.getHeight());
    }
    
    private void drawRect(Graphics g, Rectangle rect) {
        g.setColor(Color.BLACK);
        g.drawRect(OFFSET_X + rect.getX(), OFFSET_Y + rect.getY(), rect.getWidth(), rect.getHeight());
        g.setColor(Color.GREEN);
        g.fillRect(OFFSET_X + rect.getX() + 1, OFFSET_Y + rect.getY() + 1, rect.getWidth() - 1, rect.getHeight() - 1);
    }
    
    private void drawBuffer(Graphics g, Rectangle rect) {
        g.setColor(Color.YELLOW);
        g.fillRect(OFFSET_X + rect.getX(), OFFSET_Y + rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
