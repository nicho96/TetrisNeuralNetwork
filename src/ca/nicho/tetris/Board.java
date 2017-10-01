package ca.nicho.tetris;

import java.util.Random;

import ca.nicho.tetris.controller.Controller;
import ca.nicho.tetris.tiles.Tile;

public class Board {
	
	public static final int BOARD_WIDTH = 10;
	public static final int BOARD_HEIGHT = 24;
	
	public static final int HEIGHT_WEIGHT = 2;
	public static final int PLACEMENT_SCORE_WEIGHT = 10;
	public static final int CLEAR_LINE_WEIGHT = 0;
	
	public boolean isFinished;
	
	public boolean[][] tiles;
	
	public Tile currentTile;
	public int currentX;
	public int currentY;
		
	public int lastFlatnessScore = 0;
	
	public int lastScore;
	public int score;
	public long frameCount;
	
	public Controller controller;
	
	public long seed;
	
	private Random random;
	
	public Board(){
		tiles = new boolean[BOARD_WIDTH][BOARD_HEIGHT];
		this.seed = System.currentTimeMillis();
		random = new Random(seed);
		spawnNextTile();
	}
	
	public Board(long seed){
		tiles = new boolean[BOARD_WIDTH][BOARD_HEIGHT];
		this.seed = seed;
		random = new Random(seed);
		spawnNextTile();
	}
	
	public void setController(Controller controller){
		this.controller = controller;
	}
	
	public int dropTick = 0;
	public int dropRate = 3;
	
	public void tick(){
		
		frameCount ++;
		
		if(controller != null)
			controller.update();
				
		int nextX = currentX;
		int nextY = currentY;
			
		dropTick = (dropTick + 1) % dropRate;
		
		if(dropTick == 0){
			nextY += 1;
			if(hasCollision(nextX, nextY)){
				this.tilePlaced();
				spawnNextTile();
				return;
			}
					
			currentY = nextY;
		}
		
		if(controller != null){
			
			if(controller.rotatePressed){
				this.currentTile.rotateRight();
				if(hasCollision(nextX, nextY)){
					this.currentTile.rotateLeft();
				}
				controller.rotatePressed = false;
			}
			
			if(controller.rightPressed){
				nextX += 1;
				controller.rightPressed = false;
			}
			if(controller.leftPressed){
				nextX -= 1;
				controller.leftPressed = false;
			}
		}
		
		if(hasCollision(nextX, nextY)){
			return;
		}
		
		currentX = nextX;
		
		
		
	}
	
	public void spawnNextTile(){
		
		currentTile = Tile.getTile(random.nextInt(Tile.TILE_COUNT));
		
		currentX = 4;
		currentY = 0;
				
		if(hasCollision(currentX, currentY)){
			gameEnded();
			currentTile = null;
		}
		
	}
	
	public boolean hasCollision(int x, int y){
		
		if(x < 0 || x + currentTile.tile.length > tiles.length || y + currentTile.tile[0].length > tiles[0].length){
			return true;
		}
		
		for(int dx = 0; dx < currentTile.tile.length; dx++){
			for(int dy = 0; dy < currentTile.tile[0].length; dy++){
				if(currentTile.tile[dx][dy] && tiles[x + dx][y + dy])
					return true;
			}
		}
		
		return false;
	}
	
	public int checkForLines(){
		int removed = 0;
		for(int y = 0; y < Board.BOARD_HEIGHT; y++){
			boolean hasEmptyTile = false;
			for(int x = 0; x < Board.BOARD_WIDTH; x++){
				if(!tiles[x][y]){
					hasEmptyTile = true;
					break;
				}
			}
			if(!hasEmptyTile){
				removeLine(y);
				removed ++;
			}
		}
		return removed;
	}
	
	public void removeLine(int y){
		for(int i = y; i >= 0; i--){
			for(int x = 0; x < Board.BOARD_WIDTH; x++){
				if(i - 1 >= 0){
					tiles[x][i] = tiles[x][i-1];
				}else{
					tiles[x][i] = false;
				}
			}
		}
	}
	
	public void tilePlaced(){
		if(currentTile != null){
			for(int dx = 0; dx < currentTile.tile.length; dx++){
				for(int dy = 0; dy < currentTile.tile[0].length; dy++){
					if(currentTile.tile[dx][dy]){
						if(currentX + dx >= tiles.length){
							continue;
						}
						tiles[currentX + dx][currentY + dy] = true;
					}
				}
			}
		}
		
		int placementScore = getPlacementScore();
		int linesScore = checkForLines() * CLEAR_LINE_WEIGHT;
				
		lastScore = placementScore + linesScore;
		score += lastScore;
				
	}
	
	/**
	 * This score is appended to the total score of a tile placement. It provides positive/negative feedback
	 * for good/bad placements, considering empty spaces around the piece and vertical height
	 * @return the placement score
	 */
	public int getPlacementScore(){
		int delta = 0;
		for(int x = currentX; x < currentX + currentTile.tile.length && x < BOARD_WIDTH; x++){
			for(int y = currentY; y < currentY + currentTile.tile[0].length + 1 && y < BOARD_HEIGHT; y++){ //The + 1 considers the row below
				delta += (tiles[x][y]) ? 1 : -1;
			}
		}
		int score = delta * PLACEMENT_SCORE_WEIGHT - HEIGHT_WEIGHT * (Board.BOARD_HEIGHT - currentY);
		//System.out.println(score);
		return score; //Only want to return scores >= 0
	}
	
	public void gameEnded(){
		isFinished = true;
		if(controller != null){
			controller.finished();
		}
	}
	
	public void simulate(){
		while(!this.isFinished){
			tick();
		}
	}
	
}
