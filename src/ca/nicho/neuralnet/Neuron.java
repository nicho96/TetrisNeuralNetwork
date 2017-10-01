package ca.nicho.neuralnet;

import java.util.ArrayList;
import java.util.HashMap;

public class Neuron {
		
	public ArrayList<Axon> inputs = new ArrayList<Axon>(1);
	public ArrayList<Axon> outputs = new ArrayList<Axon>(1);
	
	public double value;
	
	public Layer layer;
	public int indexInLayer;
	public long neuronID;
	
	/**
	 * @param layer the layer index is this neuron a part of
	 * @param index the index of the neuron in the layer
	 */
	public Neuron(Layer layer, int index, long neuronID){
		this.layer = layer;
		this.indexInLayer = index;
		this.neuronID = neuronID;
	}
	
	public void updateValue(){
		double sum = 0;
		for(Axon a : inputs){
			sum += a.getWeightedValue();
		}
		value = NeuralNetwork.sigmoid(sum);
	}
	
}
