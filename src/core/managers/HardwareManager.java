package core.managers;

import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;

import core.employees.PhysicalDevice;

import org.lwjgl.PointerBuffer;

/**
 * @author Cezary Choduń
 *
 */
public class HardwareManager {

	public static final boolean validation = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));
	public static final ByteBuffer[] DEFAULT_VALIDATION_LAYERS = {memUTF8("VK_LAYER_LUNARG_standard_validation"), memUTF8("VK_LAYER_LUNARG_object_tracker")};
	public static final ByteBuffer[] DEFAULT_EXTENSIONS = {memUTF8(VK_KHR_SURFACE_EXTENSION_NAME)};
	
	private static VkInstance instance = null;
	private static PointerBuffer requiredExtensions = null;
	public static PhysicalDevice[] devices;
	
	/**
	 * <h5>Description:</h5>
	 * <p>Initializes GLFW resources.</p>
	 */
	public static void initialize() {
		if(!glfwInit())
			throw new RuntimeException("Failed to initialize GLFW");
		if(!glfwVulkanSupported())
			throw new AssertionError("GLFW failed to find the vulkan loader");
		
		requiredExtensions = glfwGetRequiredInstanceExtensions();
		if(requiredExtensions == null)
			throw new AssertionError("Failed to find list of required Vulkan extensions");
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Enumerate available physical devices.</p>
	 * <p><b>Note:</b> This method should be invoked only once.</p>
	 */
	public static void enumeratePhysicalDevices() {
		IntBuffer dev_count = memAllocInt(1);
		int err = vkEnumeratePhysicalDevices(instance, dev_count, null);
		if(err < 0)
			throw new AssertionError("Could not enumerate physical devices: " + Util.translateVulkanError(err));
		
		int devCount = dev_count.get(0);
		PointerBuffer pDevices = memAllocPointer(devCount);
		
		err = vkEnumeratePhysicalDevices(instance, dev_count, pDevices);
		if(err < 0)
			throw new AssertionError("Could not get physical devices: " + Util.translateVulkanError(err));
		
		devices = new PhysicalDevice[devCount];
		for(int i = 0; i < devCount; i++)
			devices[i] = new PhysicalDevice(pDevices.get(i), instance);
		
		memFree(dev_count);
		memFree(pDevices);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates default VkInstance.</p>
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 */
	public static void createDefaultInstance() {		
		createCustomInstance(EngineVersioning.getAppInfo(), DEFAULT_VALIDATION_LAYERS, DEFAULT_EXTENSIONS);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Destroys properties contained by <b><i>PhysicalDevice</i></b>(for every physical device acquired before).</p>
	 * @see {@link core.employees.PhysicalDevice}
	 */
	public static void destroyPhysicalDevicesProperties() {
		for(int i = 0; i < devices.length; i++)
			devices[i].destroyProperties();
	}
	
	/**
	 * <h5>Description:</h5>
	  * <p>
	  * 	Destroys instance.
	  * </p>
	 */
	public static void destroyInstance() {
		if(instance != null)
			vkDestroyInstance(instance, null);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates custom VkInstance.</p>
	 * @param appInfo			- VkApplicationInfo.
	 * @param validationLayers	- Validation layers that should be enabled.
	 * @param userExtensions	- Other then required by GLFW extension names.
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 * @see {@link org.lwjgl.vulkan.VkApplicationInfo}
	 */
	public static void createCustomInstance(VkApplicationInfo appInfo, ByteBuffer[] validationLayers, ByteBuffer... userExtensions) {
		if(validationLayers == null)
			throw new Error("Validation layers have not been yet specified");
		if(requiredExtensions == null)
			throw new Error("Required extensions have not been yet acquired");
		
		instance = Util.createInstance(appInfo, validationLayers, requiredExtensions, userExtensions);	
	}

	public static VkInstance getInstance() {
		return instance;
	}
}
