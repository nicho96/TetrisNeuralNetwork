package ca.nicho.neuralnet;

import java.util.ArrayList;

public class Axon {
	
	public Neuron input;
	public Neuron output;
	public double weight;
	
	/**
	 * @param neuron The neuron this axon is outputting from (Neuron)--Axon-> 
	 * @param weight The weight of this neuron with respect to the next
	 */
	public Axon(Neuron in, Neuron out, double weight){
		this.input = in;
		this.output = out;
		this.weight = weight;
	}
	
	public double getWeightedValue(){
		return weight * input.value;
	}
	
}
