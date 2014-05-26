package voxelengine;

public class RayCastCollision {
	private Block collisionBlock;
	private int face;

	private double x, y, z;

	public RayCastCollision(Block collisionBlock, int face, double x, double y, double z) {
		super();
		this.collisionBlock = collisionBlock;
		this.face = face;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Block getCollisionBlock() {
		return collisionBlock;
	}

	public void setCollisionBlock(Block collisionBlock) {
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
