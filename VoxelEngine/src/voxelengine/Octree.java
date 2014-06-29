package voxelengine;

public class Octree {
	public boolean head;
	public Octree[] children;
	
	public Octree() {
		this.head = false;
		this.children = new Octree[8];
	}
	
	public boolean getHead() {
		return head;
	}
	
	public void setHead(boolean head) {
		this.head = head;
	}
	
	public Octree getChild(byte ndx) {
		return children[ndx];
	}
	
	public void setChild(byte ndx, Octree child) {
		this.children[ndx] = child;
	}
}
