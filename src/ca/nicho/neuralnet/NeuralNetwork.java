package ca.nicho.neuralnet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class NeuralNetwork implements Comparable<NeuralNetwork> {

	
	
	public static final float ACTIVATION_THRESHOLD = 0.5F;
	public static final int RANDOM_NEURON_MAX = 3;
	public static final int RANDOM_AXON_MAX = 10;
		
	public static double sigmoid(double x) {
		return (1/( 1 + Math.pow(Math.E,(-1*x))));
	}
	
	private Random random;
	public long seed;
	
	public ArrayList<Axon> axons = new ArrayList<Axon>();
	public ArrayList<Layer> layers = new ArrayList<Layer>();
	public ArrayList<Neuron> neurons = new ArrayList<Neuron>();
	
	public int maxLayerSize;
	public int score = -1; //Negative score means it has not found one yet
	
	public int simulationCount;
	
	public Neuron[] inputs;
	public Neuron[] outputs;
	
	public boolean inputLayerCreated;
	public boolean outputLayerCreated;
	
	//Keep a cached version of the output layer available to make moving it easier
	private Layer outputLayer;
	
	/**
	 * @param inputSize Determines how many input neurons there should be
	 * @param outputSize Determines how many output neurons there are
	 * @param seed Makes the randomness seeded (and thus can be recreated later)
	 */
	public NeuralNetwork(int inputSize, int outputSize, long seed){
		inputs = new Neuron[inputSize];
		outputs = new Neuron[outputSize];
		this.seed = seed;
		random = new Random(seed);
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
					this.createNeuron(layers.get(i));
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
				this.connectNeurons(layers.get(l1).neurons.get(i1), layers.get(l2).neurons.get(i2), weight);
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
	 * @param inputSize Determines how many input neurons there should be
	 * @param outputSize Determines how many output neurons there are
	 */
	public NeuralNetwork(int inputSize, int outputSize){
		inputs = new Neuron[inputSize];
		outputs = new Neuron[outputSize];
		this.seed = System.currentTimeMillis();
		random = new Random(seed);
	}
	
	/**
	 * Populates the input array
	 */
	public void prepareInputs(){
		for(int i = 0; i < inputs.length; i++){
			inputs[i] = createNeuron(layers.get(0)); //Negative will prevent it from being added as a layer in the network.
		}
	}
	
	/**
	 * This function should only be called after all layers have been created. It will add the output neurons to the network.
	 */
	public void prepareOutputs(){
		for(int i = 0; i < outputs.length; i++){
			outputs[i] = createNeuron(outputLayer);
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
	public void connectNeurons(Neuron from, Neuron to, double weight){
		
		if(from.layer.index >= to.layer.index && to.layer.index >= 0){
			System.out.println(from.layer + " " + to.layer);
			System.out.println("WARNING: Neuron connected to same or earlier layer level. This could lead to loops");
		}
		
		Axon a = new Axon(from, to, weight);
		from.outputs.add(a);
		to.inputs.add(a);
		
		axons.add(a);
		
	}
	
	/**
	 * Create a neuron that is to be part of this network.
	 * @param layer the layer that it is being added to.
	 * @return the newly created neuron
	 */
	public Neuron createNeuron(Layer l){
		Neuron n = new Neuron(l, l.neurons.size());
		l.add(n);
		neurons.add(n);
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
	 * Create a new layer in the next available position. This will ensure the output layer is at the end.
	 */
	public Layer createNewLayer(boolean addNeuron){
		if(!inputLayerCreated || !outputLayerCreated){
			System.out.println("WARNING - Input or Output layers should be created first.");
		}
		
		layers.remove(outputLayer);
		Layer l = new Layer(layers.size());
		layers.add(l);
		outputLayer.index = layers.size();
		layers.add(outputLayer);
		
		if(addNeuron)
			createNeuron(l);
		
		return l;
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
	 * Create a copy of the network
	 * @return a copy of the network, completely independent from one another (no cross-references that would break the network)
	 */
	public NeuralNetwork copy(){
		NeuralNetwork net = new NeuralNetwork(inputs.length, outputs.length);
		
		net.createInputLayer();
		net.createOutputLayer();
				
		for(int i = 0; i < layers.size() - 2; i++){
			net.createNewLayer(false);
		}
				
		for(Neuron n : neurons){
			net.createNeuron(net.layers.get(n.layer.index));
		}
		
		for(Axon a : axons){
			int ii = a.input.index;
			int il = a.input.layer.index;
			int oi = a.output.index;
			int ol = a.output.layer.index;
			net.connectNeurons(net.layers.get(il).neurons.get(ii), net.layers.get(ol).neurons.get(oi), a.weight);
		}
		
		for(int i = 0; i < net.layers.get(0).neurons.size(); i++){
			net.inputs[i] = net.layers.get(0).neurons.get(i);
		}
		
		for(int i = 0; i < net.layers.get(net.layers.size() - 1).neurons.size(); i++){
			net.outputs[i] = net.layers.get(net.layers.size() - 1).neurons.get(i);
		}
				
		net.computeMaxLayerSize();
		
		return net;
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
	 * Generates a random network (unless NeuralNetwork.random is seeded, in which case it's constant)
	 */
	public void generateRandomNetwork(){
		
		this.createInputLayer();
		this.createOutputLayer();
		this.createNewLayer(true);
		
		prepareInputs();
		prepareOutputs();
		
		//Create x amount of neurons
		for(int i = 0; i < random.nextInt(RANDOM_NEURON_MAX); i++){
			Layer l = null;
			if(random.nextInt(30) == 0){
				l = this.createNewLayer(true);
			}else{
				l = getRandomNonIOLayer();
			}
			createNeuron(l);
		}
		
		for(int i = 0; i < random.nextInt(RANDOM_AXON_MAX); i++){
			randomNeuronConnection();
		}
		
		this.computeMaxLayerSize();
				
	}
	
	/**
	 * Utility function that will return a non input/output layer
	 * @return
	 */
	public Layer getRandomNonIOLayer(){
		return layers.get(random.nextInt(layers.size() - 2) + 1);
	}
	
	/**
	 * 
	 * @return
	 */
	public double generateSum(){
		double sum = 0;
		for(Neuron n : this.neurons){
			sum += n.value + n.inputs.size() + n.outputs.size();
		}
		for(Axon a : this.axons){
			sum += a.weight;
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
			}
			
			//Write amount of axons
			stream.writeInt(axons.size());
			
			//Write layers and indices by input and input in that order (l1, i1 -> l2, i2) for all axons followed by its weight
			for(Axon a : axons){
				stream.writeInt(a.input.layer.index);
				stream.writeInt(a.input.index);
				stream.writeInt(a.output.layer.index);
				stream.writeInt(a.output.index);
				stream.writeDouble(a.weight);
			}
			
			stream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void randomMutation(){
		
		for(int i = 0; i < random.nextInt(10) + 1; i++){
			//Determine type of mutation
			double r = random.nextDouble();
			
			if(r < 0.1){ //10% chance
				removeRandomNeuronConnection();
			}else if(r < 0.3){ //30% chance
				randomAxonWeightChange();
			}else if(r < 0.4){
				createNeuron(layers.get(random.nextInt(layers.size() - 2) + 1));
			}else if(r < 0.90){ //
				randomNeuronConnection();
			}else{
				createNewLayer(true);
			}
		}
		
	}

	private void randomNeuronConnection(){
		Layer l1 = layers.get(Math.min(random.nextInt(layers.size() - 2), random.nextInt(layers.size() - 2)));
		int r2 = random.nextInt(layers.size() - l1.index - 1) + l1.index + 1;
		Layer l2 = layers.get(r2);
		Neuron n1 = l1.neurons.get(random.nextInt(l1.neurons.size()));
		Neuron n2 = l2.neurons.get(random.nextInt(l2.neurons.size()));
		this.connectNeurons(n1, n2, random.nextDouble() * 2 - 1);
	}
	
	private void randomAxonWeightChange(){
		if(axons.size() == 0) //No axons to change
			return;
		Axon a = axons.get(random.nextInt(axons.size()));
		a.weight = random.nextDouble() * 2 - 1;
	}
	
	private void removeRandomNeuronConnection(){
		if(axons.size() == 0) //No axons to change
			return;
		Axon a = axons.get(random.nextInt(axons.size()));
		a.input.outputs.remove(a);
		a.output.inputs.remove(a);
	}

	@Override
	public String toString(){
		return this.score + "";
	}
	
	public int compareTo(NeuralNetwork other) {
		
		if(this.score < other.score){
			return -1;
		}else if(this.score > other.score){
			return 1;
		}
		
		return 0;
	}
	
	public void mutateWeights(double frequency){
		for(Axon a : axons){
			if(random.nextDouble() < frequency){
				a.weight = random.nextDouble() * 2 - 1;
			}
		}
	}
	
	public void breedWith(NeuralNetwork net, double frequency){
		for(int i = 0; i < net.axons.size(); i++){
			if(random.nextDouble() < frequency){
				this.axons.get(i).weight = net.axons.get(i).weight;
			}
		}
	}
	
	public static NeuralNetwork createPopulatedNeuralNetwork(int inputs, int outputs, int hiddenLayers, int widthOfLayers){
		
		NeuralNetwork nn = new NeuralNetwork(inputs, outputs);
		nn.createInputLayer();
		nn.createOutputLayer();
		nn.prepareInputs();
		nn.prepareOutputs();
		for(int i = 0; i < hiddenLayers; i++){
			nn.createNewLayer(false);
		}
		
		//Populate the layers
		for(int i = 1; i < nn.layers.size() - 1; i++){
			for(int k = 0; k < widthOfLayers; k++){
				nn.createNeuron(nn.layers.get(i));
			}
		}
		
		//Make the connections
		for(int i = 0; i < nn.layers.size() - 1; i++){
			for(int j = 0; j < nn.layers.get(i).neurons.size(); j++){
				for(int k = 0; k < nn.layers.get(i + 1).neurons.size(); k++){
					nn.connectNeurons(nn.layers.get(i).neurons.get(j), nn.layers.get(i + 1).neurons.get(k), nn.random.nextDouble() * 2 - 1);
				}
			}
		}
		
		return nn;
	}
			
}
