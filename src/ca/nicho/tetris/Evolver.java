package ca.nicho.tetris;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Evolver {

	public static final long BOARD_SEED = 0;
	
	public static final File DIR_PATH = new File("networks");
	
	public ArrayList<Genome> genomes = new ArrayList<Genome>();
	
	public boolean isPaused = false;
	
	public Evolver(){
		
		//new Thread(inputThread).start();
		
		for(int amount = 0; amount < 10; amount++){
			NeuralNetwork net = new NeuralNetwork(PerspectiveNeuralNetworkController.INPUT_SIZE, 3, 1);
			
			Genome genome = new Genome(net, 100, new Random());
			genome.prepareInputs(net);
			genome.prepareOutputs(net);
			genome.simulateParent();
			genome.populateInitial();
			
			genomes.add(genome);
		}
		
		runSimulation();
		
	}
	
	public void runSimulation(){
		
		if(isPaused){
			isPaused = false;
		}
		
		while(!isPaused){
			
			int bestScore = 0;
			Genome best = null;
			
			for(Genome genome : genomes){
				genome.nextGeneration();
				if(bestScore < genome.max.score){
					bestScore = genome.max.score;
					best = genome;
				}
			}
			
			System.out.println("Generation: " + best.generation + " - Fittest: " + best.max + ", sum: " + best.max.generateSum());
			
			best.max.save(new File(DIR_PATH, "1layers.dat"));
			
		}
	}
	
	public void pauseSimulation(){
		isPaused = true;
	}
	
	private Scanner sc = new Scanner(System.in);
	
	Runnable inputThread = () -> {
		System.out.print("> ");
		String input = sc.nextLine();
		if(input.toLowerCase().equals("p")){
			this.pauseSimulation();
			System.out.println("Will pause after simulation cycle ends.");
		}else if(input.toLowerCase().equals("s")){
			this.runSimulation();
			System.out.println("Resuming iterations");
		}else if(input.toLowerCase().equals("e")){
			sc.close();
			System.out.println("Will close after simulation cycle ends.");
		}
	};
	
}
