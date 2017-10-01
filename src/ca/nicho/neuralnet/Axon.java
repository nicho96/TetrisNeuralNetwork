package ca.nicho.neuralnet;

import java.util.ArrayList;

public class Axon {
	
	public Neuron input;
	public Neuron output;
	public double weight;
	public boolean enabled = true;
	public final long innovation;
	
	/**
	 * @param neuron The neuron this axon is outputting from (Neuron)--Axon-> 
	 * @param weight The weight of this neuron with respect to the next
	 */
	public Axon(Neuron in, Neuron out, double weight, long innovation){
		this.input = in;
		this.output = out;
		this.weight = weight;
		this.innovation = innovation;
	}
	
	public double getWeightedValue(){
		if(!enabled)
			return 0;
		return weight * input.value;
	}
	
}
