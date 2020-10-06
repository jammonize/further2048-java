package ai;

import game.GameLogic;
import game.Tile;

public class Expectimax {

	//Expectimax algorithim
	
	//The A.I. is constructed to favour larger numbers in particular
	//corners of the game board. Referred to as the gradient system.
	
	//Array forming weight from bottom-right vertices
    public final int[] weight1 = new int[]{
            -3,-2,-1, 0,
            -2,-1, 0, 1,
            -1, 0, 1, 2,
            0,  1, 2, 3
    };

    //Array forming weight from top-left vertices
    public final int[] weight2 = new int[]{
            3, 2, 1, 0,
            2, 1, 0,-1,
            1, 0,-1,-2,
            0,-1,-2,-3
    };
    
    //Array forming weight from top-right vertices
    public final int[] weight3 = new int[]{
            0,1,2,3,
            -1,0,1,2,
            -2,-1,0,1,
            -3,-2,-1,0
    };
    
    //Weight from bottom-left vertices
    public final int[] weight4 = new int[]{
            0,-1,-2,-3,
            1,0,-1,-2,
            2,1,0,-1,
            3,2,1,0
    };
    
    //Special gradient weight with heavier values
    public final int[] sGradient = new int[] {
            4,3,2,1,
            5,-4, -5, 0,
             6, -3, -2, -1,
             8, 10, 15, 20
    };

    //Dynamic gradient weight with heavier values
    public final int[] dGradientReset = new int[] {
            -40,-45,-50, -55,
            -35, -30, -25, -20,
            0, -5, -10, -15,
            5, 10, 15, 20
    };

    //Dynamic gradient weight
    public int[] dGradient = new int[] {
            -12,-14,-16, -18,
            -10, -8, -6, -4,
            4, 2, 0, -2,
            6, 8, 10, 12
    };

    //Extra gradient weights
    public final int[] extra1 = new int[]{
           -3,-4,-4,-5,
           -1, 0, 0, 1,
            4, 2, 2, 1,
            8,12,16,18
    };
    public final int[] extra2 = new int[]{
             0,-2,-2, 0,
             2, 1, 0, 1,
             3, 2, 1, 2,
             6, 8, 10, 12
    };

    private final int[] gOrder = new int[] {15,14,13,12,8,9,10,11,7,6,5,4,0,1,2,3};
    public int MAXDEPTH = 6;
    public boolean largeValue = false;

    //Determines potential next move
    //Depth of Expectimax search tree is dynamically altered dependent on game state
    //Score is determined using heuristic values
    public Direction nextMove(Tile[] tiles){
        int[] array = new int[4*4];
        int maxVal = 0;
        int emptyTiles = 0;
        int tilesOverThousand = 0;

        for(int i=0; i<tiles.length; i++) {
            array[i] = tiles[i].value;
            if(array[i] > maxVal)
                maxVal = array[i];
            if(array[i]==0){
                emptyTiles++;
            }
            if(array[i] > 1000)
                tilesOverThousand++;
        }
        MAXDEPTH = 6;

        if(maxVal > 2048) {
            if(emptyTiles < 5)
                MAXDEPTH = 8;
            if ( maxVal > 8000) {
                if(emptyTiles < 2 && tilesOverThousand > 1) {
                        MAXDEPTH = 10;
                } else {
                    MAXDEPTH = 8;
                }
            }
        }
       
        if(tilesOverThousand > 3 && emptyTiles < 2)
            MAXDEPTH = 12;
        if(emptyTiles > 4)
            MAXDEPTH = 6;
        if(maxVal >=8000)
            largeValue = true;
        else
            largeValue = true;

        Score score = bestScore(new Grid(array), MAXDEPTH);

        return score.direction;
    }
    
    private Score bestScore(Grid grid, int depth) {
        if(depth == 0){
            if(hasMove(grid))
                return heuristics(grid);
            else
                return new Score(0, null);
        }
        int bestScore = 0;
        Direction bestDirection = null;

        for(Direction dir : Direction.values()){

            int[][] temp = new int[4][4];
            for(int i=0; i<4*4; i++) {
                temp[i%4][(int)Math.floor(i/4)] = grid.grid[i];
            }

            boolean moved;

            if(dir == Direction.DOWN){
                moved =GameLogic.moveDown(temp);
            } else if(dir == Direction.UP)
                moved = GameLogic.moveUp(temp);
            else if(dir == Direction.LEFT)
                moved = GameLogic.moveLeft(temp);
            else
                moved = GameLogic.moveRight(temp);


            if (!moved) {
                continue;
            }
            int[] aa = new int[4*4];
            for(int x=0; x<4; x++) {
                for(int y=0; y<4; y++) {
                    aa[(y*4)+x] = temp[x][y];
                }
            }
            Grid g = new Grid(aa);

            int score = averageScore(g, depth-1);

            if(score >= bestScore) {
                bestScore = score;
                bestDirection = dir;
            }
        }
        return new Score(bestScore, bestDirection);
    }

    private int averageScore(Grid grid, int depth) {
        double totalScore = 0;
        double totalWeight = 0;
        for(Grid next : grid.getTilePossibilities()) {
            int score = bestScore(next, depth-1).score;

            totalScore += (score * next.getProbability());
            totalWeight += next.getProbability();
        }
        return (int)(totalScore/totalWeight);
    }

    private Score heuristics(Grid grid) {
        int score=0;
        for(int i=0; i<grid.grid.length;i++) {
            score += (grid.grid[i]/*grid.grid[i]*/) * extra1[i];

            if(i<15 && largeValue) {
                int curr = grid.grid[gOrder[i]];
                int next = grid.grid[gOrder[i+1]];
                if(curr<next && next>4) {
                    score -= Math.abs(next*extra1[gOrder[i+1]]);
                }
            }
        }
        return new Score(score, null);
    }

    private boolean hasMove(Grid grid) {
        for(int i : grid.grid)
            if(i==0)
                return true;

        int[][] temp = new int[4][4];
        for(int i=0; i<4*4; i++) {
            temp[i%4][(int)Math.floor(i/4)] = grid.grid[i];
        }
        if(GameLogic.moveDown(temp))
            return true;
        if(GameLogic.moveUp(temp))
            return true;
        if(GameLogic.moveLeft(temp))
            return true;
        if(GameLogic.moveRight(temp))
            return true;
        return false;
    }

    public static class Score {
        public final int score;
        public final Direction direction;
        public Score(int score, Direction direction) {
            this.score = score;
            this.direction = direction;
        }
    }
}