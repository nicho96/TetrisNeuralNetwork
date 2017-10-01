package ca.nicho.tetris;

import java.awt.Dimension;
import java.io.File;
import java.util.Random;

import javax.swing.JFrame;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Start extends JFrame {
	
	public static boolean GAME_RUNNING = true;
	
	public static boolean EVOLVING = false;
	
	public static void main(String[] s){		
		
		if(EVOLVING) new Evolver();
		else{
			Board board = new Board(Evolver.BOARD_SEED);
						
			NeuralNetwork net = new NeuralNetwork(new File("networks/1layers.dat"));
			System.out.println("Disabled Neurons: " + net.getDisabledNeuronCount());
		
			PerspectiveNeuralNetworkController controller = new PerspectiveNeuralNetworkController(board, net);
			board.setController(controller);
			Start start = new Start();
			start.setVisible(true);
			start.startWithNetwork(net, board);
		}
		
	}
	
	private Screen screen;
	
	public Start(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //TODO may want to save the state of the AI
		this.getContentPane().setLayout(null);
		this.setResizable(false);
		
		screen = new Screen();
		this.add(screen);
		
		this.setFocusable(false);
		
		this.getContentPane().setPreferredSize(new Dimension(getInsets().left + getInsets().right + screen.getWidth(), getInsets().top + getInsets().bottom + screen.getHeight()));
		this.pack();
	}
	
	public void startWithNetwork(NeuralNetwork network, Board board){
		screen.setBoard(board);
		screen.setNeuralNetwork(network);
		new Thread(screen.drawThread).start();		
	}

}
