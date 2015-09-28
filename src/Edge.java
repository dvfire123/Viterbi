package Graphs;
import java.util.*;

public class Edge {
	private Vertex in;
	private Vertex out;
	private double cost;
	
	public Edge(Vertex in, Vertex out, double cost) {
		this.in = in;
		this.out = out;
		this.cost = cost;
	}
	
	//unreachable edge
	public Edge(Vertex in, Vertex out) {
		this.in = in;
		this.out = out;
		this.cost = Double.POSITIVE_INFINITY;
	}
	
	public void changeCost(double newCost) {
		this.cost = newCost;
	}
	
	public List<Object> getEdge() {
		List<Object> e = new ArrayList<Object>(3);
		e.add(this.in);
		e.add(this.out);
		e.add(this.cost);
		
		return e;
	}
	
	public double getCost() {
		return this.cost;
	}
	
}
