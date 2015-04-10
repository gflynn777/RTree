package main;

import java.io.Serializable;


public class Rectangle implements Serializable {
	private static final long serialVersionUID = 1L;
	private int minX;
	private int minY;
	private int maxX;
	private int maxY;
	
	public Rectangle(int minX, int maxX, int minY, int maxY){
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}

	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}
	
	public boolean intersects(Rectangle rec){
	if(maxX >= rec.minX && minX <= rec.maxX && maxY >= rec.minY && minY <= rec.maxY){
		return true;
	}
	return false;
}

	@Override
	public String toString() {
		return "Rectangle [minX=" + minX + ", minY=" + minY + ", maxX=" + maxX
				+ ", maxY=" + maxY + "]";
	}
}