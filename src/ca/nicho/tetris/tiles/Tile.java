package ca.nicho.tetris.tiles;

public abstract class Tile {
	
	public static final int TILE_COUNT = 7;
	
	public boolean[][] tile;
		
	Tile(boolean[][] tile){
		this.tile = tile;
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
	
	public static Tile getTile(int value){
		
		if(value >= 7) throw new IllegalArgumentException("Value must be between 0-6 inclusive");
		
		switch(value){
			case 0: return new TileLeftL();
			case 1: return new TileLeftZ();
			case 2: return new TileRightL();
			case 3: return new TileRightZ();
			case 4: return new TileSquare();
			case 5: return new TileStraight();
			case 6: return new TileT();
		}
		return null;
	}
	
}
