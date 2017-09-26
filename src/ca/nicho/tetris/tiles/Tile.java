package ca.nicho.tetris.tiles;

import java.util.Random;

public enum Tile {
	T(new boolean[][]{{false, true}, {true, true}, {false, true}}),
	STRAIGHT(new boolean[][]{{true}, {true}, {true}, {true}}),
	RIGHTZ(new boolean[][]{{false, true, true}, {true, true, false}}),
	RIGHTL(new boolean[][]{{true, true, true}, {true, false, false}}),
	LEFTZ(new boolean[][]{{true, true, false}, {false, true, true}}),
	LEFTL(new boolean[][]{{true, false, false}, {true, true, true}}),
	SQUARE(new boolean[][]{{true, true}, {true, true}});
	
	public boolean[][] original;
	public boolean[][] tile;
		
	Tile(boolean[][] tile){
		this.tile = tile;
		this.original = tile;
	}
	
	public void rotateRight(){
		boolean[][] tmp = new boolean[tile[0].length][tile.length];
		for(int x = 0; x < tile.length; x++){
			for(int y = 0; y < tile[0].length; y++){
				tmp[tile[0].length - y - 1][x] = tile[x][y];
			}
		}
		tile = tmp;
	}
	
	public void rotateLeft(){
		boolean[][] tmp = new boolean[tile[0].length][tile.length];
		for(int x = 0; x < tile.length; x++){
			for(int y = 0; y < tile[0].length; y++){
				tmp[y][tile.length - x - 1] = tile[x][y];
			}
		}
		tile = tmp;
	}
	
	@Override
	public String toString(){
		String s = "";
		for(int y = 0; y < tile[0].length; y++){
			for(int x = 0; x < tile.length; x++){
				s += (tile[x][y]) ? "@" : " ";
			}
			s += "\n";
		}
		return s;
	}
	
}
