package ca.nicho.tetris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ca.nicho.neuralnet.Axon;
import ca.nicho.neuralnet.Layer;
import ca.nicho.neuralnet.NeuralNetwork;
import ca.nicho.neuralnet.Neuron;
import ca.nicho.tetris.controller.PerspectiveNeuralNetworkController;

public class Genome {

	public static final int KILL_THRESHOLD = 4; //The amount of the population that dies after each generation (e.g 4 will kill off 3/4 of the population)
	
	private NeuralNetwork parent;
	public NeuralNetwork max;
	
	private int genomeSize; 
	private ArrayList<NeuralNetwork> networks = new ArrayList<NeuralNetwork>();
	
	private long innovationCount;
	private long neuronCount;
		
	public int generation = 1;
	
	private Random random;
	
	public Genome(NeuralNetwork parent, int size, Random random){
		
		this.random = random;
		
		this.parent = parent;
				
		this.max = parent;
		this.genomeSize = size;
		
		networks.add(parent);
				
	}
	
	public void simulateParent(){
		if(parent.score == -1){
			simulateNetwork(parent);
		}
	}
	
	/**
	 * This will populate the network array with new slightly varied networks
	 */
	void populateInitial(){
		while(networks.size() < genomeSize){
			NeuralNetwork clone = new NeuralNetwork(parent);
			networks.add(clone);
		}
	}
	
	public void nextGeneration(){
		
		mutateGenerationAndSimulate();
		killAndRepopulateWeakest();
		
		generation++;
		
		max = networks.get(0);
				
	}
		
	public void mutateGenerationAndSimulate(){
		for(int i = 1; i < networks.size(); i++){
			NeuralNetwork nn = networks.get(i);
			
			int lastScore = nn.score;
			while(!mutateNetwork(nn)); //Wrap in a loop, to force at least 1 mutation to occure
			simulateNetwork(nn);
			
			//This will look to improve a specific mutation if it seemed favorable.
			if(nn.score > lastScore){
				int priorDelta = nn.score - lastScore;
				Genome sub = new Genome(nn, 5, random);
				sub.populateInitial();
				sub.innovationCount = this.innovationCount;
				sub.mutateGenerationAndSimulate();
				NeuralNetwork n = sub.max;
				networks.set(i, n);
				this.innovationCount = n.maxInnovation;
				int afterDelta = n.score - lastScore;
			}
			
		}
		sortNetworks();
	}
	
	/**
	 * Will cause all networks to simulate, and sort
	 */
	public void simulateGeneration(){
		
		for(NeuralNetwork n : networks){
			this.simulateNetwork(n);
		}
		
		sortNetworks();
		
	}
	
	public void sortNetworks(){
		Collections.sort(networks);
		Collections.reverse(networks);
		this.max = networks.get(0);
	}
	
	public void killAndRepopulateWeakest(){
		int rem = networks.size() / KILL_THRESHOLD;
		double a = (rem + 1) / Math.E;
		for(int i = rem; i < networks.size(); i++){
			int r1 = rem - (int)(Math.exp(random.nextDouble()) * a);
			int r2 = rem - (int)(Math.exp(random.nextDouble()) * a);
			while(r2 == r1){
				r2 = rem - (int)(Math.exp(random.nextDouble()) * a);
			}
			
			networks.set(i, breedNetwork(networks.get(r1), networks.get(r2)));
			
		}
	}
	
	private void simulateNetwork(NeuralNetwork network){
		Board b = new Board(Evolver.BOARD_SEED);
		b.setController(new PerspectiveNeuralNetworkController(b, network));
		b.simulate();
	}
	
	private boolean mutateNetwork(NeuralNetwork nn){
		double r = random.nextDouble();
		
		if(r < 0.33){
			return this.randomNeuronConnection(nn);
		}else if(r < 0.66){
			return this.randomAxonWeightChange(nn);
		}else{
			return this.splitRandomConnection(nn);
		}
			
	}
	
	/**
	 * Breed two networks together by considering their new innovations.
	 * @param fittest
	 * @param other
	 */
	private NeuralNetwork breedNetwork(NeuralNetwork fittest, NeuralNetwork other){
		//Ensures variable fittest is indeed the fittest
		if(fittest.score < other.score){
			NeuralNetwork tmp = fittest;
			fittest = other;
			other = tmp;
		}
		
		NeuralNetwork clone = new NeuralNetwork(fittest);
		
		//Mix any new innovations from the other network
		long max = Math.max(fittest.maxInnovation, other.maxInnovation);
		for(long i = 0; i < max; i++){
			if(fittest.axonsMap.containsKey(i) && other.axonsMap.containsKey(i)){
				
				//Weight is inherited by the most fit network, but whether or not it's enabled can be inherited by either (by chance)
				clone.axonsMap.get(i).enabled = (random.nextBoolean()) ? fittest.axonsMap.get(i).enabled : other.axonsMap.get(i).enabled;
				
			}else if(other.axonsMap.containsKey(i)){
				Axon gene = other.axonsMap.get(i);
				
				Neuron node = clone.neuronsMap.get(gene.output.neuronID);
				
				//If the gene's output neuron doesn't exist, create it
				if(node == null){
					node = clone.createNeuron(clone.layers.get(gene.output.layer.index), gene.output.neuronID);
				}
				
				//Make the connection
				clone.connectNeurons(clone.neuronsMap.get(gene.input.neuronID), node, gene.weight, gene.innovation);
				
			}else{
				//Innovation does not exist in either networks. Ignore
			}
		}
		
		//For any disabled axons, 25% chance of being reenabled (https://www.cs.cmu.edu/afs/cs/project/jair/pub/volume21/stanley04a-html/node3.html)
		for(Axon a : clone.axons){
			if(!a.enabled){
				if(random.nextDouble() < 0.25){
					a.enabled = true;
				}
			}
		}
		
		return clone;
		
	}
	
	public boolean splitRandomConnection(NeuralNetwork nn){
		
		//Only axons that are separated by two or more layers should be considered.
		ArrayList<Axon> possibilities = new ArrayList<Axon>();
		for(Axon a : nn.axons){
			if(a.output.layer.index - a.input.layer.index > 1 && a.enabled){
				possibilities.add(a);
			}
		}
		
		if(possibilities.size() == 0){
			return false; //No possibilities
		}
		
		Axon a = possibilities.get(random.nextInt(possibilities.size()));
		
		//Disable this connection, as it's about to be split. We do not remove it however, as it may mutate later.
		a.enabled = false; 
		
		//Create neuron directly in the layer between both (e.g input->layer 2 output->layer 6 will place the neuron in (6-2) / 2 + 2 = 4 after integer division)
		Neuron neuron = nn.createNeuron(nn.layers.get((a.output.layer.index - a.input.layer.index) / 2 + a.input.layer.index), neuronCount++);
		
		//The connection from the input starts off with weight 1 (this actually keeps the functionality of the network identical, until there is a mutation).
		nn.connectNeurons(a.input, neuron, 1, innovationCount++);
				
		//The connection to the output maintains the weight of the original connection
		nn.connectNeurons(neuron, a.output, a.weight, innovationCount++);
		
		return true;
		
	}
	
	public void randomAxonToggle(NeuralNetwork nn){
		if(nn.axons.size() == 0)
			return;
		Axon a = nn.axons.get(random.nextInt(nn.axons.size()));
		a.enabled = !a.enabled;
	}

	public boolean randomNeuronConnection(NeuralNetwork nn){	
		
		//Get the non-empty layers from the array
		ArrayList<Layer> possibilities = new ArrayList<Layer>();
		for(Layer l : nn.layers){
			if(l.neurons.size() > 0){
				possibilities.add(l);
			}
		}
		
		//There are not two layers available to create a random neuron connection
		if(possibilities.size() < 2){
			return false;
		}
		
		//Get a split index
		int splitIndex = random.nextInt(possibilities.size() - 1);
		
		//Get neurons before the split
		ArrayList<Neuron> left = new ArrayList<Neuron>();
		for(int i = 0; i <= splitIndex; i++){
			for(Neuron n : possibilities.get(i).neurons){
				left.add(n);
			}
		}
		
		//Get neurons after the split
		ArrayList<Neuron> right = new ArrayList<Neuron>();
		for(int i = splitIndex + 1; i < possibilities.size(); i++){
			for(Neuron n : possibilities.get(i).neurons){
				right.add(n);
			}
		}
		
		Neuron n1 = left.get(random.nextInt(left.size()));
		Neuron n2 = right.get(random.nextInt(right.size()));
		nn.connectNeurons(n1, n2, random.nextDouble() * 2 - 1, innovationCount++);
		
		return true;
		
	}
	
	public boolean randomAxonWeightChange(NeuralNetwork nn){
		if(nn.axons.size() == 0) //No axons to change
			return false;
		Axon a = nn.axons.get(random.nextInt(nn.axons.size()));
		a.weight = random.nextDouble() * 2 - 1;
		return true;
	}
	
	/**
	 * Populates the input array
	 */
	public void prepareInputs(NeuralNetwork nn){
		for(int i = 0; i < nn.inputs.length; i++){
			nn.inputs[i] = nn.createNeuron(nn.layers.get(0), neuronCount++);
		}
	}
	
	/**
	 * This function should only be called after all layers have been created. It will add the output neurons to the network.
	 */
	public void prepareOutputs(NeuralNetwork nn){
		for(int i = 0; i < nn.outputs.length; i++){
			nn.outputs[i] = nn.createNeuron(nn.outputLayer, neuronCount++);
		}
	}
	
	
}