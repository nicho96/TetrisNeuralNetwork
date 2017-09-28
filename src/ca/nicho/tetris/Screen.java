package ca.nicho.tetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JPanel;

import ca.nicho.neuralnet.Axon;
import ca.nicho.neuralnet.Layer;
import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Neuron;

public class Screen extends JPanel {

	public static final int TILE_SIZE = 25;
	public static final int FRAME_WIDTH = TILE_SIZE * Board.BOARD_WIDTH;
	public static final int FRAME_HEIGHT = TILE_SIZE * Board.BOARD_HEIGHT;
	
	private BufferedImage image;
	private int[] raster;
	
	public Board board;
	
	public boolean showOverlay = true;
	
	public NeuralNetwork network;
	
	public void setNeuralNetwork(NeuralNetwork network){
		this.network = network;
	}
	
	public Screen(){
		this.setSize(FRAME_WIDTH + 800, FRAME_HEIGHT);
		image = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
		raster = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();		
		this.setFocusable(true);
	}
	
	public void setBoard(Board board){
		this.board = board;
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		g.drawImage(image, 0, 0, null);
		
		if(network != null && showOverlay){
			drawPerspectiveNetwork(g);

//			drawNetwork(g);
		}
		
	}
	
	public void drawPerspectiveNetwork(Graphics g){
		//Write the input neurons state
		int dy = FRAME_HEIGHT / network.maxLayerSize; //Vertical distance between neurons
		int dx = 20;
				
		for(int i = 0; i < network.inputs.length; i++){
			Neuron n = network.inputs[i];
			int x = i % Board.BOARD_WIDTH;
			int y = i / Board.BOARD_WIDTH;
			if(n.value == 0){
				g.setColor(Color.LIGHT_GRAY);
			}else if(n.value == 1){
				g.setColor(Color.GREEN);
			}else{
				g.setColor(Color.ORANGE);
			}
			int xPos = x * TILE_SIZE + (TILE_SIZE - 10) / 2;
			int yPos = (y + board.currentY) * TILE_SIZE + (TILE_SIZE - 10) / 2;
			g.fillOval(xPos, yPos, 10, 10);
			
			for(Axon a : n.outputs){
				if(a.output.layer.index > 0){
					if(a.weight < 0){
						g.setColor(Color.BLACK);
					}else{
						g.setColor(Color.GRAY);
					}
					g.drawLine(xPos + 5, yPos + 5, FRAME_WIDTH + dx + a.output.layer.index * dx + 5, dy * a.output.index + 5);
				}
			}
			
		}
		
		//Start at 1, layer 0 is reserved for the inputs
				for(int i = 1; i < network.layers.size(); i++){
					Layer l =  network.layers.get(i);
					int xOff = FRAME_WIDTH + dx + i * dx;
					for(int j = 0; j < l.neurons.size(); j++){
						Neuron n = l.neurons.get(j);
						int yOff = dy * j;
						if(n.value > 0.5){
							g.setColor(Color.GREEN);
						}else{
							g.setColor(Color.RED);
						}
						g.fillOval(xOff, yOff, 10, 10);
						
						for(Axon a : n.outputs){
							if(a.output.layer.index >= 0){
								if(a.weight < 0){
									g.setColor(Color.DARK_GRAY);
								}else{
									g.setColor(Color.white);
								}
								g.drawLine(xOff + 5, yOff + 5, FRAME_WIDTH + dx + a.output.layer.index * dx + 5, dy * a.output.index + 5);
							}
						}
						
					}
				}
				g.setColor(Color.orange);
				g.drawString("Score " + board.score, getWidth() - 100, 10);
				g.drawString("Score " + board.lastScore, getWidth() - 100, 30);
		
	}
	
	public void drawNetwork(Graphics g){
		if(true) return;
		
		int dx = 20;
		int dy = FRAME_HEIGHT / network.maxLayerSize; //Vertical distance between neurons
		
		//Write the input neurons state
		for(int i = 0; i < network.inputs.length; i++){
			Neuron n = network.inputs[i];
			int x = i % Board.BOARD_WIDTH;
			int y = i / Board.BOARD_WIDTH;
			if(n.value == 0){
				g.setColor(Color.LIGHT_GRAY);
			}else if(n.value == 1){
				g.setColor(Color.GREEN);
			}else{
				g.setColor(Color.ORANGE);
			}
			int xPos = x * TILE_SIZE + (TILE_SIZE - 10) / 2;
			int yPos = y * TILE_SIZE + (TILE_SIZE - 10) / 2;
			g.fillOval(xPos, yPos, 10, 10);
			
			for(Axon a : n.outputs){
				if(a.output.layer.index > 0){
					if(a.weight < 0){
						g.setColor(Color.BLACK);
					}else{
						g.setColor(Color.GRAY);
					}
					g.drawLine(xPos + 5, yPos + 5, FRAME_WIDTH + dx + a.output.layer.index * dx + 5, dy * a.output.index + 5);
				}
			}
			
		}
		
		//Start at 1, layer 0 is reserved for the inputs
		for(int i = 1; i < network.layers.size(); i++){
			Layer l =  network.layers.get(i);
			int xOff = FRAME_WIDTH + dx + i * dx;
			for(int j = 0; j < l.neurons.size(); j++){
				Neuron n = l.neurons.get(j);
				int yOff = dy * j;
				if(n.value > 0.5){
					g.setColor(Color.GREEN);
				}else{
					g.setColor(Color.RED);
				}
				g.fillOval(xOff, yOff, 10, 10);
				
				for(Axon a : n.outputs){
					if(a.output.layer.index >= 0){
						if(a.weight < 0){
							g.setColor(Color.DARK_GRAY);
						}else{
							g.setColor(Color.white);
						}
						g.drawLine(xOff + 5, yOff + 5, FRAME_WIDTH + dx + a.output.layer.index * dx + 5, dy * a.output.index + 5);
					}
				}
				
			}
		}
		g.setColor(Color.orange);
		g.drawString("Score " + board.score, 10, 10);
	}
	
	public void drawGrid(){
		for(int x = 0; x < FRAME_WIDTH; x ++){
			for(int y = 0; y < FRAME_HEIGHT; y ++){
				if(x % TILE_SIZE == 0 || y % TILE_SIZE == 0){
					raster[x + y * FRAME_WIDTH] = 0xAAAAAA;
				}
			}
		}
	}
	
	public void clearGraphics(){
		for(int i = 0; i < raster.length; i++)
			raster[i] = 0xFFFFFF;
	}
	
	public void drawBoard(){
		//Draw the current board state
		for(int x = 0; x < Board.BOARD_WIDTH; x++){
			for(int y = 0; y < Board.BOARD_HEIGHT; y++){
				if(board.tiles[x][y]) drawTile(x, y, 0xFF0000);
			}
		}
	}
	
	public void drawActiveTile(){
		if(board.currentTile == null)
			return;
		
		for(int x = 0; x < board.currentTile.tile.length; x++){
			for(int y = 0; y < board.currentTile.tile[0].length; y++){
				if(board.currentTile.tile[x][y]) drawTile(board.currentX + x, board.currentY + y, 0x0000FF);
			}
		}
	}
	
	public void drawTile(int x, int y, int color){
		for(int dx = 0; dx < TILE_SIZE; dx++){
			for(int dy = 0; dy < TILE_SIZE; dy++){
				drawPixel(x * TILE_SIZE + dx, y * TILE_SIZE + dy, color);
			}
		}
	}
	
	public void drawPixel(int x, int y, int color){
		raster[x + FRAME_WIDTH * y] = color;
	}
	
	public void updateScreen(){
		clearGraphics();
		drawBoard();
		drawActiveTile();
		drawGrid();
		repaint();
	}
	
	public Runnable drawThread = new Runnable() {
		public void run(){
			long last = System.currentTimeMillis();
			
			while(Start.GAME_RUNNING){
				long current = System.currentTimeMillis();
				if(current - last > 10){
										
					if(board == null){
						continue;
					}
										
					if(!board.isFinished)
						board.tick();
					
					board.controller.update(); //Will allow the game to render properly
					
					updateScreen();
					
					last = current;
					
				}
			}
		}
	};
	
}
