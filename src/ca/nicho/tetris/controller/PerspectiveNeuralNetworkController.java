package ca.nicho.tetris.controller;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.tetris.Board;

public class PerspectiveNeuralNetworkController extends Controller {

	public static final int DISTANCE_AHEAD = 5;
	public static final int MAX_PIECE_WIDTH = 4;
	public static final int INPUT_SIZE = (DISTANCE_AHEAD + MAX_PIECE_WIDTH) * Board.BOARD_WIDTH;
	
	public NeuralNetwork network;
	
	/**
	 * Create a neural network controller with a specified network
	 * @param board
	 * @param network
	 */
	public PerspectiveNeuralNetworkController(Board board, NeuralNetwork network){
		super(board);
		this.network = network;
		board.setController(this);
	}

	/**
	 * Converts the board state into a set of signals
	 * @return the board state
	 */
	public double[] boardToInputs(){
		
		double[] inputs = new double[(DISTANCE_AHEAD + MAX_PIECE_WIDTH) * Board.BOARD_WIDTH]; 		
		
		//Inputs for the active tile
		for(int dx = 0; dx < board.currentTile.tile.length; dx++){
			for(int dy = 0; dy < board.currentTile.tile[0].length; dy++){
				if(board.currentTile.tile[dx][dy]){
					inputs[(board.currentX + dx) + dy * Board.BOARD_WIDTH] = -1.0;
				}
			}
		}
				
		//Inputs for the already placed tiles
		for(int x = 0; x < board.tiles.length; x++){
			for(int y = 0; y < DISTANCE_AHEAD + MAX_PIECE_WIDTH; y++){
				if(board.currentY + y < board.tiles[0].length){
					if(board.tiles[x][board.currentY + y]){
						inputs[x + y * Board.BOARD_WIDTH] = 1.0;
					}
				}else{
					inputs[x + y * Board.BOARD_WIDTH] = 1.0;
				}
			}
		}
		
		return inputs;
	}
	
	@Override
	public void update() {
				
		if(board.isFinished)
			return;
		
		network.score = board.score; //Update the network's score
		
		double[] inputs = boardToInputs();
		network.updateInputs(inputs);
		network.updateLayers();
		
		if(!board.isFinished){
			if(network.outputs[0].value > NeuralNetwork.ACTIVATION_THRESHOLD){
				leftPressed = true;
			}
			
			if(network.outputs[1].value > NeuralNetwork.ACTIVATION_THRESHOLD){
				rightPressed = true;
			}
			
			if(network.outputs[2].value > NeuralNetwork.ACTIVATION_THRESHOLD){
				rotatePressed = true;
			}
		}else{
			System.out.println("This network's score: " + network.score);
		}
		
	}
	
	public void finished(){
		network.score = board.score;
	}
	
	public void showOutputValues(){
		for(int i = 0; i < network.outputs.length; i++){
			System.out.print(network.outputs[i].value + " ");
		}
		
		System.out.println();
	}
	
}
