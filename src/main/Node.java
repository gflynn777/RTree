package main;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	private int pageNum;
	private ArrayList<Tuple> tupleList;//null for non-leaf page
	private ArrayList<Node> nodeList;//only used temporarily to calculate mbr
	private Rectangle mbr;
	private boolean isLeaf;
	private int startChildPage;
	private int lastChildPage;
	private int level;
	
	//Constructor used for leaf pages
	public Node(int pageNum){
		this.pageNum = pageNum;
		tupleList = new ArrayList<Tuple>() ;
		isLeaf = true;
		nodeList = null;
	}
	//Constructor used for index pages
	public Node(int pageNum, boolean isLeaf){
		this.pageNum = pageNum;
		nodeList = new ArrayList<Node>();
		isLeaf = false;
		tupleList = null;
	}
	

	public void clearNodes(){
		nodeList.clear();
		if (tupleList != null)
			tupleList.clear();
		tupleList = null;
		nodeList = null;
	}
	
	public void clearTuples(){
		tupleList.clear();
		tupleList = null;
	}
	
	public void setLevel(int i){
		level = i;
	}
	public int getLevel(){
		return level;
	}
	
	public int getStartChildPage() {
		return startChildPage;
	}
	
	public void setStartChildPage(int startChildPage) {
		this.startChildPage = startChildPage;
	}
	
	public int getLastChildPage() {
		return lastChildPage;
	}
	public void setLastChildPage(int lastChildPage) {
		this.lastChildPage = lastChildPage;
	}
	
	public boolean isLeaf(){
		return isLeaf;
	}
	
	public int getNumTuples(){
		return tupleList.size();
	}
	
	public int getNumNodes(){
		return nodeList.size();
	}
	
	public void addTuple(Tuple tup){
		tupleList.add(tup);
	}
	
	public Tuple getTuple(int i){
		return tupleList.get(i);
	}
	
	public void addChild(Node node){
		nodeList.add(node);
	}
	
	public Node getChild(int i){
		return nodeList.get(i);
	}
	
	public Rectangle getMbr(){
		return mbr;
	}
	
	public int getPageNum(){
		return pageNum;
	}
	
	public void setMbr(Rectangle rec){
		this.mbr = rec;
	}
	
	public Rectangle calculateMbr(){
		int maxPt1 = 0;
		int minPt1 = this.tupleList.get(0).getX();
		int maxPt2 = 0;
		int minPt2 = this.tupleList.get(0).getY();
		for (Tuple tup : tupleList){
			if (tup.getX() > maxPt1)
				maxPt1 = tup.getX();
			if (tup.getY() > maxPt2)
				maxPt2 = tup.getY();
			if (tup.getX() < minPt1)
				minPt1 = tup.getX();
			if (tup.getY() < minPt2)
				minPt2 = tup.getY();
		}
		mbr = new Rectangle(minPt1, maxPt1, minPt2, maxPt2);
		return mbr;
	}
	
	public Rectangle calculateNodeMbr(){
		int maxPt1 = 0;
		int minPt1 = this.nodeList.get(0).getMbr().getMinX();
		int maxPt2 = 0;
		int minPt2 = this.nodeList.get(0).getMbr().getMinY();

		for (Node node : nodeList){
			if (node.getMbr() == null)
				System.out.println("null mbr");
			if (node.getMbr().getMaxX() > maxPt1)
				maxPt1 = node.getMbr().getMaxX();
			if (node.getMbr().getMaxY() > maxPt2)
				maxPt2 = node.getMbr().getMaxY();
			if (node.getMbr().getMinX() < minPt1)
				minPt1 = node.getMbr().getMinX();
			if (node.getMbr().getMinY() < minPt2)
				minPt2 = node.getMbr().getMinY();
		}
		mbr = new Rectangle(minPt1, maxPt1, minPt2, maxPt2);
		return mbr;
	}

}
