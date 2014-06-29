package voxelengine;

public class RayCastCollision {
	private byte collisionBlock;
	private int face;

	private double x, y, z;
	
	public RayCastCollision(byte collisionBlock, int face, double x, double y, double z) {
		super();
		this.collisionBlock = collisionBlock;
		this.face = face;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public byte getCollisionBlock() {
		return collisionBlock;
	}

	public void setCollisionBlock(byte collisionBlock) {
		this.collisionBlock = collisionBlock;
	}

	public int getFace() {
		return face;
	}

	public void setFace(int face) {
		this.face = face;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
}
