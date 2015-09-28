import java.util.*;

//The Trellis Graph
//Note: the number of states is fixed!
//Rate of convolutional code = 1/(log2(Ns) + 1)

//Structure:
/*
 * Root (numDepth = 0)
 * List of vertices in window 1
 * List of vertices in window 2
 * ...
 * List of vertices in window numDepth-2
 * Goal
 * 
 */
public class Trellis {
	public static final int NUM_STATES = 2;
	private int numDepth;
	private List<List<Vertex>> Vlist;
	private List<List<Edge>> Elist;
	
	//Viterbi parameters
	private double[] minCost;
	private double[] sMetric;
	private int[] bestPath;
	
	public static void test() {
		
		//Set up trellis
		int depth = 4;
		Trellis t = new Trellis(depth);
		System.out.println("Vertices:");
		t.print();
		
		if (!t.changeCost(0, 0, 0, 0)) {
			System.out.println("Value not changed!");
		}
		
		if (!t.changeCost(0, 1, 0, 1)) {
			System.out.println("Value not changed!");
		}
		
		if (!t.changeCost(0, 0, 1, 4)) {
			System.out.println("Value not changed!");
		}
		
		if (!t.changeCost(1, 1, 1, 1)) {
			System.out.println("Value not changed!");
		}
		
		if (!t.changeCost(0, 0, 2, 0)) {
			System.out.println("Value not changed!");
		}
		
		if (!t.changeCost(1, 0, 2, 1)) {
			System.out.println("Value not changed!");
		}
		
		if (!t.changeCost(1, 0, 1, 0)) {
			System.out.println("Value not changed!");
		}
		
		System.out.println();
		System.out.println("Edges:");
		t.printEdgeCost();
		
		//run Viterbi
		double bestCost = t.vitDecoder();
		System.out.println("\nLowest cost is: " + bestCost);
		System.out.println("The lowest cost path is: " + Arrays.toString(t.bestPath));
	}
	
	public Trellis(int depth) {
		this.numDepth = depth;
		if (depth == 0) {
			return;
		}
		
		addAllVertices();
		addAllEmptyEdges();
		
		this.bestPath = new int[numDepth];
		this.minCost = new double[numDepth];
		this.sMetric = new double[NUM_STATES];
	}
	
	private void addAllVertices() {
		Vlist = new ArrayList<List<Vertex>> ();
		
		String term = Integer.toBinaryString(0);	//the terminating string
		
		Vertex rootV = new Vertex(term);
		List<Vertex> V = new ArrayList<Vertex>();
		V.add(rootV);
		Vlist.add(V);
		
		if (numDepth == 1) {
			//we only have the root node
			return;
		}
		
		//Add all vertices
		Vertex goalV = new Vertex(term);
		
		for (int d = 1; d < numDepth-1; d++) {
			V = new ArrayList<Vertex>();
			for (int n = 0; n < NUM_STATES; n++) {
				String sym = Integer.toBinaryString(n);
				Vertex v = new Vertex(sym);
				V.add(v);
			}
			
			Vlist.add(V);
		}
		
		V = new ArrayList<Vertex>();
		V.add(goalV);
		Vlist.add(V);
	}
	
	private void addAllEmptyEdges() {
		//empty edge: edge of cost +ve infinity
		Elist = new ArrayList<List<Edge>> ();
		
		for (int i = 0; i < numDepth-1; i++) {
			int numNodesCurr = Vlist.get(i).size();
			int numNodesNext = Vlist.get(i+1).size();
			List<Edge> E = new ArrayList<Edge>();
			
			for (int nc = 0; nc < numNodesCurr; nc++) {
				Vertex in = Vlist.get(i).get(nc);
				
				for (int nn = 0; nn < numNodesNext; nn++) {
					//add empty edge here
					Vertex out = Vlist.get(i+1).get(nn);
					Edge e = new Edge(in, out);
					E.add(e);
				}
			}
			
			Elist.add(E);
		}
	}
	
	private void print() {
		//print out the entire Trellis
		for (int d = 0; d < numDepth; d++) {
			for (int n = 0; n < Vlist.get(d).size(); n++) {
				Vertex v = Vlist.get(d).get(n);
				String s = v.getVal();
				
				System.out.print(s + " ");
			}
			
			System.out.println();
		}
	}
	
	private void printEdgeCost() {
		for (int d = 0; d < numDepth-1; d++) {
			for (int n = 0; n < Elist.get(d).size(); n++) {
				Edge e = Elist.get(d).get(n);
				double cost = e.getCost();
				
				System.out.print(cost + " ");
			}
			
			System.out.println();
		}
	}
	
	private boolean changeCost(int nc, int nn, int from, double newCost) {
		//given the starting window (from)
		//and the from's node (nc) and (from+1)'s nn, change the 
		//cost of the edge
		//returns true if cost successfully changed
		
		if (from >= numDepth-1 || nc*nn >= NUM_STATES*NUM_STATES) {
			return false;
		}
		
		int vcSize = Vlist.get(from).size();
		int vnSize = Vlist.get(from+1).size();
		
		if (nc >= vcSize || nn >= vnSize) {
			return false;
		}
		
		int edgeIndx = nc*vnSize + nn;
		
		Edge e = Elist.get(from).get(edgeIndx);
		e.changeCost(newCost);
		
		return true;
	}
	
	/**The Viterbi algorithm**/
	private double vitDecoder() {
		//returns the minimum weight
		
		/*Initializations*/
		for (int i = 0; i < NUM_STATES; i++) {
			sMetric[i] = 0;
		}
		
		minCost[0] = 0;
		
		/*dp*/
		for (int s = 1; s < numDepth - 1; s++) {
			double[] tempMetric = new double[NUM_STATES];
			
			for (int n = 0; n < NUM_STATES; n++) {
				//update the branch metric
				double minVal = Double.POSITIVE_INFINITY;
				for (int m = 0; m < NUM_STATES; m++) {
					int vSize = Vlist.get(s).size();
					
					int edgeIndx;
					if (s == 1) {
						edgeIndx = n;
					} else {
						edgeIndx = m*vSize + n;
					}
					
					Edge e = Elist.get(s-1).get(edgeIndx);
					double val = sMetric[m] + e.getCost();
					
					if (val < minVal) {
						minVal = val;
					}
				}
				
				tempMetric[n] = minVal;
			}
			
			double minVal = Double.POSITIVE_INFINITY;
			for (int n = 0; n < NUM_STATES; n++) {
				sMetric[n] = tempMetric[n];
				
				if (sMetric[n] < minVal) {
					minVal = sMetric[n];
				}
			}
			
			minCost[s] = minVal;
		}
		
		//Finally compute the goal node
		double minVal = Double.POSITIVE_INFINITY;
		for (int n = 0; n < NUM_STATES; n++) {
			Edge e = Elist.get(numDepth-2).get(n);
			double cost = e.getCost();
			double val = sMetric[n] + cost;
			
			if (val < minVal) {
				minVal = val;
			}
		}
		
		minCost[numDepth - 1] = minVal;
		traceback();
		
		return minVal;
	}
	
	//The traceback to find the best path
	private void traceback() {
		//Given the minCost array is already filled
		
		//init: root and goal nodes
		bestPath[0] = 0;
		bestPath[numDepth-1] = 0;
		
		//tracing back
		int curV = 0;
		for (int s = numDepth - 2; s > 0; s--) {
			double diff = minCost[s+1] - minCost[s];
			boolean diffFlag = false;
			
			int vSize = Vlist.get(s).size();
			int vNextSize = Vlist.get(s+1).size();
			double minEdge = Double.POSITIVE_INFINITY;
			int minIndx = 0;
			
			for (int n = 0; n < vSize; n++) {
				int edgeIndx = n*vNextSize + curV;
				double val = Elist.get(s).get(edgeIndx).getCost();
				
				if (val == diff) {
					curV = n;
					bestPath[s] = curV;
					diffFlag = true;
					break;
				}
				
				if (val < minEdge) {
					minEdge = val;
					minIndx = n;
				}
			}
			
			if (!diffFlag) {
				curV = minIndx;
				bestPath[s] = minIndx;
			}
		}
	}
}
