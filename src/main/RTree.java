package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RTree implements Serializable {
	private static final long serialVersionUID = 1L;
	static List<Tuple> tupleList = new ArrayList<Tuple>();
	private static File file;
	private static ArrayList<Tuple> queryResult = new ArrayList<Tuple>();
	private static int pageAccesses = 0;
	private static int maxNodeFill = (int)Math.floor((double)4096/(double)36);
	private static int maxPageFill = (int)Math.floor((double)4096/(double)1036);
	private static int currentPage = 0;
	private static boolean write = true;
	private static HashMap<Integer, Node> map = new HashMap<Integer, Node>();
	private static Node root;
	private static Node[] leaves;
	private static boolean firstRun = true;
	
	//Maximize Function
	private static int xMax = 0;
	private static int yMax = 0;
	private static double ptA = 0;
	private static double ptB = 0;
	private static double maximum = 0;
	
	//For Testing
	private static ArrayList<Tuple> actualsInQuery = new ArrayList<Tuple>();
	private static Rectangle query = new Rectangle(30, 8024, 30, 401);
	
	public static void main(String[] args){
		File f = new File(".");
		System.out.println(f.getAbsolutePath());
		if (args.length != 1 || !(new File(args[0]).exists())){
			System.out.println("Please run in format: java -cp bin main.RTree <testFilePath>");
			System.exit(1);
		}
		else
			file = new File(args[0]);
		
		try {
			//Read in numbers
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine()) != null){
				String[] pts = line.split(",");
				Tuple tup = new Tuple(Integer.parseInt(pts[0]), Integer.parseInt(pts[1]));
				tupleList.add(tup);
				if (tup.isContainedBy(query))
					actualsInQuery.add(tup);
				
			}	
			//Sort the array
			Collections.sort(tupleList);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Build the tree and run the main program
		leaves = initPages();
		root = bulkLoad(leaves, currentPage);
		getInput();
	}
	
	//Keep asking user for more min's and max's until they want to quit
	public static void getInput(){
		Scanner in = new Scanner(System.in);
		String input = null;
		String[] inputs = null;

		do{
			System.out.println("Input Instructions:");
			System.out.println("M <NewSize>    Reset the max index node size.");
			System.out.println("P\t Print the results of the previous query.");
			System.out.println("N\t Start a new query.");
			System.out.println("F\t Find the points that maximize a function (ax + by)");
			System.out.println("Q\t Quit the program.");
			input = in.nextLine();
			inputs = input.toUpperCase().split(" ");
			if (inputs.length == 2 && inputs[0].equals("M") && isInteger(inputs[1])){
				if (inputs[1].equals("1")){
					System.out.println("Must enter number greater than 1!");
					continue;
				}
				maxNodeFill = Integer.parseInt(inputs[1]);
				System.out.println("Re-Creating Tree Structure:");
				root = bulkLoad(leaves, currentPage);
				getInput();
				return;
			}
			else if (input.equalsIgnoreCase("P"))
				printQueryResults();
			else if (input.equalsIgnoreCase("F"))
				extraCredit();
			else if (input.equalsIgnoreCase("N"))
				doQuery();
					
		}while(!input.equalsIgnoreCase("Q"));
	}
	
	public static void doQuery(){
		Scanner in = new Scanner(System.in);
		String[] pt;
		boolean test = false;
		queryResult.clear();
		pageAccesses = 0;
		do{
			System.out.println("Please enter minimum point (in the form x,y): ");
			String pt1 = in.nextLine();
			pt = pt1.split(",");
			if (pt.length == 2)
				test = isInteger(pt[1].trim());
		}while(!(isInteger(pt[0].trim()) && test));
		int minX = Integer.parseInt(pt[0].trim());
		int minY = Integer.parseInt(pt[1].trim());
		test = false;
		do{
			System.out.println("Please enter maximum point (enter the same numbers for a point query): ");
			String pt2 = in.nextLine();
			pt = pt2.split(",");
			if (pt.length == 2)
				test = isInteger(pt[1].trim());
		}while(!(isInteger(pt[0].trim()) && test));
		int maxX = Integer.parseInt(pt[0].trim());
		int maxY = Integer.parseInt(pt[1].trim());
		test = false;
		Rectangle query = new Rectangle(minX, maxX, minY, maxY);
		System.out.println("Input accepted. Calculating...");
		rangeQuery(root, query);
		System.out.println("\n"+queryResult.size() +" points within query.");
		System.out.println("Page Accesses: "+pageAccesses);
	}
	
	/**1. Initialize a page (a node in node array)
	 * 		a. set starting tuple id
	 * 2. Take a tuple out of the sorted tupleList
	 * 3. Set the tuple's description
	 * 4. Keep adding while page is not full
	 * 		a. set last tuple id
	 * 5. Initialize a new page and repeat starting at where you left off
	 */
	public static Node[] initPages(){
		int pageNum = 0;
		Node[] leafNode = new Node[(int)Math.ceil((double)tupleList.size() / (double)maxPageFill)];
		System.out.println("Writing leaf pages to disk (may take up to a minute)...");
		for (int i = 0; i < tupleList.size(); ){
			
			//Initialize the leaf page
			leafNode[pageNum] = new Node(pageNum);
			leafNode[pageNum].setStartChildPage(i);
			//Fill it up
			while(leafNode[pageNum].getNumTuples() < maxPageFill && i < tupleList.size()){
				Tuple tup = tupleList.get(i);
				tup.addDescription();
				leafNode[pageNum].addTuple(tup);
				i++;
			}
			leafNode[pageNum].calculateMbr();
			leafNode[pageNum].setLastChildPage(i-1);
			if (write)
				writeToDisk(leafNode[pageNum]);
			pageNum++;
			if (pageNum % 200 == 0)
				System.out.print(".");
		}
		System.out.println("\nCreating Tree Structure:");
		currentPage = pageNum;
		return leafNode;
	}
	
	//Recursively add node sub-arrays to the tree
	public static Node bulkLoad(Node[] previousLevel, int pageNum){
		int j = 0;
		int level = previousLevel[0].getLevel()+1;
		if (firstRun){
			level = 1;
			firstRun = false;
		}
		Node[] nodes = new Node[(int) Math.ceil((double)previousLevel.length / (double) maxNodeFill)];//r/n pages
		for (int i=0; i < nodes.length; i++){
			
			//Initialize the page
			nodes[i] = new Node(pageNum++, false);
			nodes[i].setLevel(level);
			nodes[i].setStartChildPage(previousLevel[j].getPageNum());
			
			//Fill the Node
			while (nodes[i].getNumNodes() < maxNodeFill && j < previousLevel.length){
					nodes[i].addChild(previousLevel[j]);
				j++;
			}
			nodes[i].setLastChildPage(previousLevel[j-1].getPageNum());
			nodes[i].calculateNodeMbr();
			nodes[i].clearNodes();
			map.put(nodes[i].getPageNum(), nodes[i]);
		}
		System.out.println(j+ " nodes in "+nodes.length+ " pages");
		if (nodes.length > 1)
			return bulkLoad(nodes, pageNum);//Returns the page of the root
		else
			return nodes[0];
	}
	
	/**
	 * 	Given a query rectangle, an R-Tree is able to quickly find all entries that are
	 *	contained within the query rectangle or which overlap the query rectangle
	 *	This method recursively iterates through the node mbr's entering into all mbr's that
	 *	contain the query.
	 */
	public static void rangeQuery(Node x, Rectangle rec){
		Node currNode;
		if (x.getLevel() > 1){
			for (int i = x.getStartChildPage(); i <= x.getLastChildPage(); i++){
				//If its an index page we want to get its sub-indexes
				if(x.getMbr().intersects(rec)){
					currNode = map.get(i);
					pageAccesses++;
					rangeQuery(currNode, rec);
				}
			}
		}
		else if (x.getLevel() == 1){
			//Children are leaf nodes on disk
			for (int j=x.getStartChildPage(); j <= x.getLastChildPage(); j++){//iterate through level above leaf level
				currNode = readFromDisk(j);
				pageAccesses++;
				for (int i=0; i<maxPageFill && i<currNode.getNumTuples(); i++){
					if (currNode.getTuple(i).isContainedBy(rec))
						queryResult.add(currNode.getTuple(i));
				}
			}
		}
	}
 
	//Check if a given number is an integer
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	//Check if a given string is a double
	public static boolean isDouble(String s){
		try{
			Double.parseDouble(s);
		} catch(NumberFormatException e){
			return false;
		}
		return true;
	}
	
	//Write a node object to disk
	public static void writeToDisk(Node node){
		try{
			FileOutputStream fout = new FileOutputStream("./temp/"+node.getPageNum());
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(node);
			oos.close();
	 
	   }catch(Exception ex){
		   ex.printStackTrace();
	   }
	}
	
	//Read a node object from disk
	public static Node readFromDisk(int pageNum){
	   try{
		   FileInputStream fin = new FileInputStream("./temp/"+pageNum);
		   ObjectInputStream ois = new ObjectInputStream(fin);
		   Node node = (Node) ois.readObject();
		   ois.close();
 
		   return node;
 
	   }catch(Exception ex){
		   ex.printStackTrace();
		   return null;
	   } 
	}
	
	//Print the results of a query
	public static void printQueryResults(){
		System.out.println("Query Results:");
		if (queryResult.size() == 0)
			System.out.println("none");
		for (Tuple tup : queryResult)
			System.out.println(tup);
	}
	
	//Gets the a and b values from the user
	public static void extraCredit(){
		//Get two points from user and make sure they add to 1
		Scanner in = new Scanner(System.in);
		String a,b;
		boolean first = true;
		maximum = 0;
		do{
			if(!first)
				System.out.println("Values must add up to 1! Try again.");
			do{
					System.out.println("Please enter a value for a: ");
					a = in.nextLine();
				}while(!isDouble(a));
		
				do{
					System.out.println("Please enter a value for b: ");
					b = in.nextLine();
				}while(!isDouble(b));
				first = false;
		}while((ptA = Double.parseDouble(a)) + (ptB = Double.parseDouble(b)) != 1);
		//Traverse tree to see exactly which point maximizes the function
		findMax(root);
		System.out.println("Maximum Values: "+xMax+", "+yMax);
	}
	
	//Finds the (ax + by) value of a point
	public static double maximize(int x, int y){
		return (double)x*ptA + (double)y*ptB;
	}

	//Recursively searches through the tree to find the point that maximizes the function
	public static void findMax(Node x){
		Node currNode;
		double currentHighest = 0;
		Rectangle highestRec = null;
		if (x.getLevel() > 1){
			for (int i = x.getStartChildPage(); i <= x.getLastChildPage(); i++){
				if(maximize(x.getMbr().getMaxX(), x.getMbr().getMaxY()) >= currentHighest){
						currentHighest = maximize(x.getMbr().getMaxX(), x.getMbr().getMaxY());
						highestRec = x.getMbr();
				}
			}
			for(int i=x.getStartChildPage(); i<=x.getLastChildPage(); i++){
				if (x.getMbr().intersects(highestRec)){
					currNode = map.get(i);
					findMax(currNode);
				}
			}
		}
		else if (x.getLevel() == 1){
			//Children are leaf nodes on disk
			for (int j=x.getStartChildPage(); j <= x.getLastChildPage(); j++){//iterate through level above leaf level
				currNode = readFromDisk(j);
				for (int i=0; i<maxPageFill && i<currNode.getNumTuples(); i++){
					if (maximize(currNode.getTuple(i).getX(), currNode.getTuple(i).getY()) >= maximum){
						maximum = maximize(currNode.getTuple(i).getX(), currNode.getTuple(i).getY());
						xMax = currNode.getTuple(i).getX();
						yMax = currNode.getTuple(i).getY();
					}
				}
			}
		}
	}
}
/**Sizing:
 * Leaf Node: integers(x, y, maxX, maxY, minX, minY, pageNum, level) = 8
 * 			  booleans(isLeaf) = 1
 * 			  char[](description) = 500
 * Total Size = 8(4) + 1(4) + 500(2) = 1036
 * 
 * Index Node: integers(maxX, maxY, minX, minY, pageNum, startChildPage, lastChildPage, level) = 8
 * 			   booleans(isLeaf) = 1
 * Total Size = 8(4) + 1(4) = 36
 * 
 * Notes: Java uses 4 bytes to represent booleans pointers and integers
 *  	  and 2 bytes to represent a char.
 */