package voxelengine;

public class Camera {
	public double x;
	public double y;
	public double z;
	public double rotX;
	public double rotY;
	public double rotZ;
	public double fovH;
	public double fovV;
	
	// constructor
	public Camera(double x, double y, double z, double rotY, double rotZ, double rotX, double fovH, double fovV) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.fovH = fovH;
		this.fovV = fovV;
	}
}
