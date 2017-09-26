package ca.nicho.neuralnet;

import java.util.ArrayList;
import java.util.HashMap;

public class Neuron {
		
	public ArrayList<Axon> inputs = new ArrayList<Axon>(1);
	public ArrayList<Axon> outputs = new ArrayList<Axon>(1);
	
	public double value;
	
	public Layer layer;
	public int index;
	
	/**
	 * @param layer the layer index is this neuron a part of
	 * @param index the index of the neuron in the layer
	 */
	public Neuron(Layer layer, int index){
		this.layer = layer;
		this.index = index;
	}
	
	public void updateValue(){
		double sum = 0;
		for(Axon a : inputs){
			sum += a.getWeightedValue();
		}
		value = NeuralNetwork.sigmoid(sum);
	}
	
}
