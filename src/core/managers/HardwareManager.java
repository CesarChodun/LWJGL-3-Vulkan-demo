package core.managers;

import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.glfw.GLFW.*;

import java.nio.ByteBuffer;

import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.PointerBuffer;

/**
 * @author Cezary Choduń
 *
 */
public class HardwareManager {

	public static final boolean validation = Boolean.parseBoolean(System.getProperty("vulkan.validation", "false"));
	
	private static VkInstance instance = null;
	private static PointerBuffer requiredExtensions = null;
	
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
	 * <p>Creates default VkInstance.</p>
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 */
	public static void createDefaultInstance() {
		ByteBuffer[] defaultValidationLayers = {memUTF8("VK_LAYER_LUNARG_standard_validation"), memUTF8("VK_LAYER_LUNARG_object_tracker")};
		ByteBuffer[] defaultExtensions = {memUTF8(VK_KHR_SURFACE_EXTENSION_NAME)};
		
		createCustomInstance(EngineVersioning.getAppInfo(), defaultValidationLayers, defaultExtensions);
		
		Util.freeBuffers(defaultValidationLayers);
		Util.freeBuffers(defaultExtensions);
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
