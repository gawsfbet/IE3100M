/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

/**
 *
 * @author Kevin-Notebook
 */
public class CplexSolution {
    private final int numBoxes;
    private final BoxArrangement[] boxArrangements;
    
    public CplexSolution(int numBoxes, BoxArrangement[] boxArrangements) {
        this.numBoxes = numBoxes;
        this.boxArrangements = boxArrangements;
    }
    
    public int getNumBoxes() {
        return this.numBoxes;
    }
    
    public BoxArrangement[] getBoxArrangements() {
        return this.boxArrangements;
    }
}
