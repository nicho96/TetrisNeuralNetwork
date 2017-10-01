package ca.nicho.neuralnet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class NeuralNetwork implements Comparable<NeuralNetwork> {

	public static final float ACTIVATION_THRESHOLD = 0.5F;
	public static final int RANDOM_NEURON_MAX = 3;
	public static final int RANDOM_AXON_MAX = 10;
		
	public static double sigmoid(double x) {
		return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}

	public ArrayList<Axon> axons = new ArrayList<Axon>();
	public HashMap<Long, Axon> axonsMap = new HashMap<Long, Axon>();
	
	public ArrayList<Layer> layers = new ArrayList<Layer>();
	
	public ArrayList<Neuron> neurons = new ArrayList<Neuron>();
	public HashMap<Long, Neuron> neuronsMap = new HashMap<Long, Neuron>();
	
	public int maxLayerSize = 1;
	public int score = -1; //Negative score means it has not found one yet
	
	public int simulationCount;
	
	public ArrayList<Neuron> hidden = new ArrayList<Neuron>();
	public Neuron[] inputs;
	public Neuron[] outputs;
	
	public boolean inputLayerCreated;
	public boolean outputLayerCreated;
	
	public long maxInnovation;
	
	//Keep a cached version of the output layer available to make moving it easier
	public Layer outputLayer;
	
	/**
	 * Creates a new blank neural network
	 * @param inputSize Determines how many input neurons there should be
	 * @param outputSize Determines how many output neurons there are
	 * @param hiddenLayers Determines how many hidden layers exist
	 */
	public NeuralNetwork(int inputSize, int outputSize, int hiddenLayers){
		inputs = new Neuron[inputSize];
		outputs = new Neuron[outputSize];
		
		this.createInputLayer();
		this.createHiddenLayers(hiddenLayers);
		this.createOutputLayer();
	}
	
	/**
	 * Creates a carbon copy of a neural network, without sharing any memory references (are functionally independent)
	 * @param parent the network to be cloned
	 */
	public NeuralNetwork(NeuralNetwork parent){
		
		inputs = new Neuron[parent.inputs.length];
		outputs = new Neuron[parent.outputs.length];
		
		createInputLayer();
		createHiddenLayers(parent.layers.size() - 2);
		createOutputLayer();
				
		for(Neuron n : parent.neurons){
			createNeuron(layers.get(n.layer.index), n.neuronID);
		}
				
		for(Axon a : parent.axons){
			int ii = a.input.indexInLayer;
			int il = a.input.layer.index;
			int oi = a.output.indexInLayer;
			int ol = a.output.layer.index;
			connectNeurons(layers.get(il).neurons.get(ii), layers.get(ol).neurons.get(oi), a.weight, a.enabled, a.innovation);
		}
		
		for(int i = 0; i < layers.get(0).neurons.size(); i++){
			inputs[i] = layers.get(0).neurons.get(i);
		}
		
		for(int i = 0; i < layers.get(layers.size() - 1).neurons.size(); i++){
			outputs[i] = layers.get(layers.size() - 1).neurons.get(i);
		}
				
		this.score = parent.score;
		
		computeMaxLayerSize();
				
	}
	
	/**
	 * Load a neural network from a valid binary file
	 * @param f the file to be loaded
	 */
	public NeuralNetwork(File f){
		try {
			DataInputStream input = new DataInputStream(new FileInputStream(f));
						
			//Read amount of layers
			int layerCount = input.readInt();
			for(int i = 0; i < layerCount; i++){
				layers.add(new Layer(i));
			}
			
			//Layers are loaded from the save file
			this.inputLayerCreated = true;
			this.outputLayerCreated = true;
			
			outputLayer = layers.get(layers.size() - 1);

			
			//Read the amount of neurons per layer, and create them
			for(int i = 0; i < layerCount; i++){
				int neuronCount = input.readInt();
				for(int j = 0; j < neuronCount; j++){
					long neuronID = input.readLong();
					this.createNeuron(layers.get(i), neuronID);
				}
			}
			
			//Get amount of axons
			int axonCount = input.readInt();
			
			//Get layers and indices by input and input in that order (l1, i1 -> l2, i2) for all axons followed by its weight
			for(int i = 0; i < axonCount; i++){
				int l1 = input.readInt();
				int i1 = input.readInt();
				int l2 = input.readInt();
				int i2 = input.readInt();
				double weight = input.readDouble();
				boolean enabled = input.readBoolean();
				long innovation = input.readLong();
				this.connectNeurons(layers.get(l1).neurons.get(i1), layers.get(l2).neurons.get(i2), weight, enabled, innovation);
			}
			
			input.close();
			
			//Setup the input neurons
			inputs = new Neuron[layers.get(0).neurons.size()];
			for(int i = 0; i < layers.get(0).neurons.size(); i++){
				inputs[i] = layers.get(0).neurons.get(i);
			}
			
			//Setup the output neurons
			outputs = new Neuron[layers.get(layers.size() - 1).neurons.size()];
			for(int i = 0; i < layers.get(layers.size() - 1).neurons.size(); i++){
				outputs[i] = layers.get(layers.size() - 1).neurons.get(i);
			}
			
			this.computeMaxLayerSize();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @param outputs this array is expected to be the same length as specified by inputSize in the constructor.
	 */
	public void updateInputs(double[] in){
		for(int i = 0; i < in.length; i++){
			inputs[i].value = in[i];
		}
	}
	
	/**
	 * updates the values of each neuron, starting from lowest to highest layers
	 */
	public void updateLayers(){
		for(int i = 1; i < layers.size(); i++){
			Layer l = layers.get(i);
			for(Neuron n : l.neurons){
				n.updateValue();
			}
		}
	}

	/**
	 * Connect two neurons to one another, with an axon in between
	 * @param from input neuron
	 * @param to output neuron
	 * @param weight how much this connection should be weighed.
	 */
	public void connectNeurons(Neuron from, Neuron to, double weight, boolean enabled, long innovation){
		
		if(from.layer.index >= to.layer.index && to.layer.index >= 0){
			System.out.println("WARNING: Neuron connected to same or earlier layer level. This could lead to loops");
		}
		
		Axon a = new Axon(from, to, weight, innovation);
		a.enabled = enabled;
		from.outputs.add(a);
		to.inputs.add(a);
		
		axonsMap.put(innovation, a);
		axons.add(a);
		
		if(maxInnovation < innovation){
			innovation = maxInnovation;
		}
		
	}
	
	/**
	 * Connect two neurons to one another, with an axon in between
	 * @param from input neuron
	 * @param to output neuron
	 * @param weight how much this connection should be weighed.
	 * @param innovation the innovation number of the axon
	 */
	public void connectNeurons(Neuron from, Neuron to, double weight, long innovation){
		connectNeurons(from, to, weight, true, innovation);
	}
	
	/**
	 * Create a neuron that is to be part of this network.
	 * @param layer the layer that it is being added to.
	 * @return the newly created neuron
	 */
	public Neuron createNeuron(Layer l, long neuronID){
		Neuron n = new Neuron(l, l.neurons.size(), neuronID);
		l.add(n);
		neurons.add(n);
		neuronsMap.put(neuronID, n);
		if(l.index != 0 && l.index != layers.size() - 1){
			hidden.add(n);
		}
		
		return n;
	}
	
	/**
	 * Creates the input layer. Neurons will /not/ be populated here.
	 */
	public void createInputLayer(){
		if(inputLayerCreated){
			System.out.println("WARNING - Input layer already created. Ignoring this function call.");
			return;
		}
		Layer l = new Layer(0);
		layers.add(l.index, l);
		inputLayerCreated = true;
	}
	
	/**
	 * Create the hidden layers. This should be called before creating the output layer
	 */
	public void createHiddenLayers(int amount){
		for(int i = 0; i < amount; i++){
			layers.add(new Layer(layers.size()));
		}
	}
	
	/**
	 * Creates the output layer. Neurons will /not/ be populated here.
	 */
	public void createOutputLayer(){
		if(outputLayerCreated){
			System.out.println("WARNING - Output layer already created. Ignoring this function call.");
			return;
		}
		outputLayer = new Layer(layers.size());
		layers.add(outputLayer.index, outputLayer);
		outputLayerCreated = true;
	}
		
	/**
	 * Utility function that will find the max layer size, and store it in the maxLayerSize variable
	 * Primarily used for rendering the neurons. Layer 0 is excluded as it is reserved for the inputs
	 */
	public void computeMaxLayerSize(){
		for(int i = 1; i < layers.size(); i++){
			Layer l = layers.get(i);
			if(l.neurons.size() > this.maxLayerSize){
				this.maxLayerSize = l.neurons.size();
			}
		}
	}

	/**
	 * Prints basic info about the network
	 */
	public void printInfo(){
		System.out.println("Number of neurons: " + neurons.size());
		System.out.println("Number of layers: " + layers.size());
		System.out.println("Number of axons: " + axons.size());
		System.out.print("[");
		for(int i  = 0; i < layers.size(); i++){
			System.out.print("{layer=" + i + ", size=" + layers.get(i).neurons.size() + "}");
			if(i < layers.size() - 1){
				System.out.print(", ");
			}
		}
		System.out.println("]");
	}
	
	/**
	 * Computes a sum value of the network. It's very unlikely that two networks share the same sum, unless they are identical networks (though not impossible)
	 * @return
	 */
	public double generateSum(){
		double sum = 0;
		for(Neuron n : this.neurons){
			sum += n.inputs.size() + n.outputs.size();
		}
		for(Axon a : this.axons){
			if(a.enabled)
				sum += Math.abs(a.weight);
		}
		return sum;
	}
	
	public void save(File f){
		try {
			
			if(!f.exists()){
				f.createNewFile();
			}
			
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(f));
			
			//Write the amount of layers
			stream.writeInt(layers.size());
			
			//Write number of neurons per layer
			for(Layer l : layers){
				stream.writeInt(l.neurons.size());
				for(Neuron n : l.neurons){
					stream.writeLong(n.neuronID);
				}
			}
			
			//Write amount of axons
			stream.writeInt(axons.size());
			
			//Write layers and indices by input and input in that order (l1, i1 -> l2, i2) for all axons followed by its weight
			for(Axon a : axons){
				stream.writeInt(a.input.layer.index);
				stream.writeInt(a.input.indexInLayer);
				stream.writeInt(a.output.layer.index);
				stream.writeInt(a.output.indexInLayer);
				stream.writeDouble(a.weight);
				stream.writeBoolean(a.enabled);
				stream.writeLong(a.innovation);
			}
			
			stream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public int compareTo(NeuralNetwork other) {
		
		if(this.score < other.score){
			return -1;
		}else if(this.score > other.score){
			return 1;
		}else{
			if(this.generateSum() > other.generateSum()){
				return -1;
			}else if(this.generateSum() < other.generateSum()){
				return 1;
			}
		}
		
		return 0;
	}
	
	public int getDisabledNeuronCount(){
		int count = 0;
		for(Axon a : axons){
			if(!a.enabled){
				count ++;
			}
		}
		return count;
	}
	
	@Override
	public String toString(){
		return this.score + "";
	}
	
}
