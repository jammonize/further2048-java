package ai;

import game.GameLogic;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    
	public int[] grid;

    private double probability = 1;

    public Grid(int[] grid) {
        this.grid = grid;
    }

    public Grid(int[] grid, double probability) {
        this.grid = grid;
        this.probability = probability;
    }

    public Direction[] getPossibleMoves() {
        ArrayList<Direction> dirAL = new ArrayList<Direction>();
        for(Direction newDir : Direction.values()){
            if(GameLogic.simulateMove(new Grid(grid.clone(), probability), newDir)!= null)
                dirAL.add(newDir);
        }
        return dirAL.toArray(new Direction[dirAL.size()]);
    }

    public List<Grid> getTilePossibilities() {
        ArrayList<Grid> possAL = new ArrayList<Grid>();
        for(int i=0; i<16;i++) {
            if(grid[i]==0) {
                int[] copy = new int[4*4];
                int[] copy2= new int[4*4];
                System.arraycopy(grid, 0, copy, 0, 4*4);
                System.arraycopy(grid, 0, copy2, 0, 4*4);
                copy[i] = 2;
                copy2[i] = 4;
                possAL.add(new Grid(copy, 0.9));
                possAL.add(new Grid(copy2, 0.1));
            }
        }
        return possAL;
    }

    public double getProbability() {
        return probability;
    }
}