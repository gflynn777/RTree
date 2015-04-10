package main;

import java.io.Serializable;

public class Tuple implements Serializable, Comparable<Tuple> {
	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	private int tupleId;
	private char[] description;
	
	public Tuple(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	public int getTupleId() {
		return tupleId;
	}

	public void setTupleId(int tupleId) {
		this.tupleId = tupleId;
	}

	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public char[] getDescription() {
		return description;
	}
	
	public void setDescription() {
		this.description = fillDescription();
	}
	
	public void addDescription(){
		this.description = fillDescription();
	}
	
	public char[] fillDescription(){
		char[] description = new char[500];
		for (int i=0; i < 500; i++)
			description[i] = 'a';
		return description;
	}
	
	public long getHilbertValue() {
    	int BITS_PER_DIM = 32;
        long res = 0;
        int x1 = x;
        int x2 = y;

        for (int ix = BITS_PER_DIM - 1; ix >= 0; ix--) {
            long h = 0;
            long b1 = (x1 & (1 << ix)) >> ix;
            long b2 = (x2 & (1 << ix)) >> ix;

            if (b1 == 0 && b2 == 0) {
                h = 0;
            } else if (b1 == 0 && b2 == 1) {
                h = 1;
            } else if (b1 == 1 && b2 == 0) {
                h = 3;
            } else if (b1 == 1 && b2 == 1) {
                h = 2;
            }
            res += h << (2 * ix);
        }
        return res;
    }
	
	public boolean equals(Tuple tup){
		if (this.x == tup.getX() && this.y == tup.getY())
			return true;
		return false;
	}
	
	public boolean isContainedBy(Rectangle rec){
		if(!(x <= rec.getMaxX() && x >= rec.getMinX() && y >= rec.getMinY() && y <= rec.getMaxY())){
			//System.out.println(this +" is not contained by "+ rec);
			return false;
		}
		return true;
			
	}

	@Override
	public int compareTo(Tuple tuple) {
		Tuple tup = (Tuple) tuple;
		if (this.getHilbertValue() > tup.getHilbertValue())
			return 1;
		else if (this.getHilbertValue() < tup.getHilbertValue())
			return -1;
		else 
			return 0;
	}

	@Override
	public String toString() {
		return "Tuple ["+x + ", "+ y + " descr[499]=" + description[499]+ "]";
	}
}
