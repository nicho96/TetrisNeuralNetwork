package ca.nicho.neuralnet;

import java.util.ArrayList;

public class Layer {

	public ArrayList<Neuron> neurons = new ArrayList<Neuron>();
	
	public int index;
	
	public Layer(int layer){
		this.index = layer;
	}
	
	public void add(Neuron n){
		neurons.add(n);
	}
	
}
