package core.employees;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * <h5>Description:</h5>
 * <p>Class for creating window using <b>GLFW</b>.</p>
 * @author Cezary Choduń
 *
 */
public class Window {
	
	public static final int DEFAULT_WIDTH = 1920, DEFAULT_HEIGHT = 1080, DEFAULT_X_CORD = 0, DEFAULT_Y_CORD = 0;
	public static final String DEFAULT_NAME = "Window";

	private int width, height, x, y;
	private String name;
	private long windowID;
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with default name and dimensions.</p>
	 * 
	 */
	public Window() {
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		x = DEFAULT_X_CORD;
		y = DEFAULT_Y_CORD;
		
		name = DEFAULT_NAME;
		createWindow();
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with default dimensions and custom name.</p>
	 * 	@param name	- Name of the window.
	 */
	public Window(String name) {
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		x = DEFAULT_X_CORD;
		y = DEFAULT_Y_CORD;
		
		this.name = name;
		createWindow();
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with custom dimensions.</p>
	 * 
	 * 	@param x  		- Horizontal distance between top left monitor corner and top left window corner.
	 * 	@param y 		- Vertical distance between top left monitor corner and top left window corner.
	 * 	@param width	- Width of the created window.
	 * 	@param height	- Height of the created window. 
	 */
	public Window(int x, int y, int height, int width) {		
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		
		this.name = DEFAULT_NAME;
		createWindow();
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with custom name and dimensions.</p>
	 * 
	 * 	@param x  		- Horizontal distance between top left monitor corner and top left window corner.
	 * 	@param y 		- Vertical distance between top left monitor corner and top left window corner.
	 * 	@param width	- Width of the created window.
	 * 	@param height	- Height of the created window. 
	 * 	@param name		- Name of the window.
	 */
	public Window(int x, int y, int height, int width, String name) {		
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		
		this.name = name;
		createWindow();
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates a window.</p>
	 */
	private void createWindow() {
		windowID = glfwCreateWindow(width, height, name, NULL, NULL);
		setPos(x, y);
	}
	
	
	public void setVisible(boolean val) {
		if(val)
			glfwShowWindow(windowID);
		else
			glfwHideWindow(windowID);
	}
	public boolean isVsible() {
		return ((glfwGetWindowAttrib(windowID, GLFW_VISIBLE) == 0)? true : false);
	}
	public void setPos(int x, int y) {
		this.x = x; this.y = y;
		glfwSetWindowPos(windowID, x, y);
	}
	public void setSize(int width, int height) {
		this.width = width; this.height = height;
		glfwSetWindowSize(windowID, width, height);
	}
	public void setBounds(int x, int y, int width, int height) {
		setPos(x, y);
		setSize(width, height);
	}
	
	public long getWindowID() {
		return windowID;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public String getName() {
		return name;
	}
}
