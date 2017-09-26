package ca.nicho.tetris;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.tetris.controller.NeuralNetworkController;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Evolver {

	public static final long BOARD_SEED = 0;
	
	public static final File DIR_PATH = new File("networks");
	
	public Evolver(){
		perspectiveNetwork();
	}
	
	public void perspectiveNetwork(){
		
		long time = System.currentTimeMillis();
		int iteration = 0;
		
		ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
				
		for(int i = 0; i < 20; i++){
			NeuralNetwork net = new NeuralNetwork(PerspectiveNeuralNetworkController.INPUT_SIZE, 3);
			net.generateRandomNetwork();
			networks.add(net);
		}
		
		while(iteration < 100000){
			
			long startTime = System.currentTimeMillis();
			
			for(int ind = 0; ind < networks.size(); ind++){
			
				NeuralNetwork localMax = networks.get(ind);
				
				//Simulate if need be
				if(localMax.score == -1){
					Board b = new Board(BOARD_SEED);
					b.setController(new PerspectiveNeuralNetworkController(b, localMax));
					b.simulate();
				}
			
				for(int i = 0; i < 1000; i++){
					NeuralNetwork net = localMax.copy();
					net.randomMutation();
					
					Board b = new Board(BOARD_SEED);
					b.setController(new PerspectiveNeuralNetworkController(b, net));
					b.simulate();	
									
					if(net.score > localMax.score){
						localMax = net;
					}
				
				}
				
				if(localMax == networks.get(ind)){
					localMax.simulationCount += 1;
					if(localMax.simulationCount == 5){
						NeuralNetwork net = new NeuralNetwork(PerspectiveNeuralNetworkController.INPUT_SIZE, 3);
						net.generateRandomNetwork();
						networks.set(ind, net);
					}
				}else{
					networks.set(ind, localMax);
				}
				
				if(localMax == networks.get(ind)){
					System.out.print((localMax == networks.get(ind)) ? "*" : "+");
				}
				
			}
			
			long endTime = System.currentTimeMillis();
			
			System.out.println(" " + (endTime - startTime) / 1000 + "s to finish");
			
			Collections.sort(networks);
			Collections.reverse(networks);
	
			NeuralNetwork maxNet = networks.get(0);
			
			File f = new File(DIR_PATH, time + "-net.dat");
			File f2 = new File(DIR_PATH, time + "-net2.dat");
			maxNet.save(f);
			maxNet.save(f2); //Save 2 so at least 1 will be intact
			
			System.out.println("Done iteration " + iteration + ". Max score: " + maxNet.score + " simulationCount: " + maxNet.simulationCount + ", sum: " + maxNet.generateSum());
			
			iteration++;
			
		}
		
	}
	
	public void fullNetwork(){
		
		int iteration = 0;
		
		ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
		
		for(int i = 0; i < 20; i++){
			networks.add(NeuralNetwork.createPopulatedNeuralNetwork(Board.BOARD_HEIGHT * Board.BOARD_WIDTH, 3, 1, Board.BOARD_HEIGHT * Board.BOARD_WIDTH));
		}
						
		while(iteration < 100000){
			
			NeuralNetwork maxNet = null;
			int max = 0;
			int sum = 0;
						
			for(int ind = 0; ind < networks.size(); ind++){
			
				//Copy and create a random mutation of the board
				NeuralNetwork network = networks.get(ind);
				
				Board b = new Board(BOARD_SEED);
				b.setController(new NeuralNetworkController(b, network));
				b.simulate();		
				
				if(network.score > max){
					maxNet = network;
					max = network.score;
				}
				
				sum += network.score;
									
				System.out.print("*");
								
			}
			
			System.out.println();
			
			Collections.sort(networks);
			Collections.reverse(networks);
						
			for(int i = networks.size() / 10; i < networks.size(); i++){
				NeuralNetwork mutated = networks.get((int)(Math.random() * 10)).copy();
				if(Math.random() < 0.8){
					mutated.breedWith(networks.get((int)(Math.random() * networks.size())), 0.5);
				}
				mutated.mutateWeights(0.1);
				networks.set(i, mutated);
			}
			
			File f = new File(DIR_PATH, "net.dat");
			File f2 = new File(DIR_PATH, "net2.dat");
			maxNet.save(f);
			maxNet.save(f2); //Save 2 so at least 1 will be intact
			
			System.out.println("Done iteration " + iteration + ". Max score: " + maxNet.score + " simulationCount: " + maxNet.simulationCount + ", sum: " + maxNet.generateSum() + ", average: " + (sum / networks.size()));
			
			iteration++;
			
		}
		
		
	}
	
	public NeuralNetwork randomNetwork(){
		NeuralNetwork net = new NeuralNetwork(Board.BOARD_HEIGHT * Board.BOARD_WIDTH, 3);
		net.generateRandomNetwork();
		return net;
	}
	
}
