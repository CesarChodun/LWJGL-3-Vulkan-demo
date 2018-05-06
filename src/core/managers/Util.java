package core.managers;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.Buffer;

import core.employees.PhysicalDevice;
import core.employees.LogicalDevice;

import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.PointerBuffer;

/**
 * @author cezarychodun
 *
 */
public class Util {
	
	public static ByteBuffer[] makeByteBuffers(String[] texts) {
		ByteBuffer[] out = new ByteBuffer[texts.length];
		
		for(int i = 0; i < texts.length; i++)
			out[i] = memUTF8(texts[i]);
		
		return out;
	}
	
	public static PointerBuffer makePointer(ByteBuffer[] buffers) {
		PointerBuffer pb = memAllocPointer(buffers.length);
		
		for(ByteBuffer b : buffers)
			pb.put(b);
		pb.flip();
		
		return pb;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates <code><b><i>LogicalDevice</i></b></code> with given parameters.</p>
	 * @param dev					- <b>Must</b> be a valid <code><b><i>PhysicalDevice</i></b></code>.
	 * @param requiredQueueFlags	- Bitwise <b>OR</b> of required queue flags.
	 * @param layers				- Layers to be enabled.
	 * @param extensions			- Needed extensions.
	 * @return	<p>Valid LogicalDevice.</p>
	 * @see {@link core.employees.LogicalDevice}
	 */
	public static LogicalDevice createLogicalDevice(PhysicalDevice dev, int requiredQueueFlags, String[] layers, String[] extensions) {
		ByteBuffer[] bLayers = makeByteBuffers(layers);
		ByteBuffer[] bExtensions = makeByteBuffers(extensions);
		PointerBuffer pExtensions = makePointer(bExtensions);
		
		LogicalDevice logicalDevice = createLogicalDevice(dev, requiredQueueFlags, bLayers, pExtensions);
		
		memFree(pExtensions);
		
		freeBuffers(bLayers);
		freeBuffers(bExtensions);
		
		return logicalDevice;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates <code><b><i>LogicalDevice</i></b></code> with given parameters.</p>
	 * @param dev					- <b>Must</b> be a valid <code><b><i>PhysicalDevice</i></b></code>.
	 * @param requiredQueueFlags	- Bitwise <b>OR</b> of required queue flags.
	 * @param layers				- Layers to be enabled.
	 * @param extensions			- Needed extensions.
	 * @return	<p>Valid LogicalDevice.</p>
	 * @see {@link core.employees.LogicalDevice}
	 */
	public static LogicalDevice createLogicalDevice(PhysicalDevice dev, int requiredQueueFlags, ByteBuffer[] layers, PointerBuffer extensions) {
		LogicalDevice out = null;
		
		int desiredQueueIndex = dev.getNextQueueFamilyIndex(0, requiredQueueFlags);
		
		FloatBuffer queuePriorities = memAllocFloat(1).put(0.0f);
		queuePriorities.flip();
		
		PointerBuffer ppEnabledLayerNames = makePointer(layers);

		VkDeviceQueueCreateInfo.Buffer deviceQueueCreateInfo = VkDeviceQueueCreateInfo.calloc(1)
				.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
				.pNext(NULL)
				.queueFamilyIndex(desiredQueueIndex)
				.pQueuePriorities(queuePriorities);
		
		VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.pQueueCreateInfos(deviceQueueCreateInfo)
				.ppEnabledLayerNames(ppEnabledLayerNames)
				.ppEnabledExtensionNames(extensions);
		
		PointerBuffer pdev = memAllocPointer(1);
		int err = vkCreateDevice(dev, deviceCreateInfo, null, pdev);
		if(err < 0)
			throw new AssertionError("Failed to create logical device: " + translateVulkanError(err));
		
		long ldev = pdev.get(0);
		out = new LogicalDevice(ldev, dev, deviceCreateInfo);
		out.setGraphicsQueueFamilyIndex(desiredQueueIndex);

		deviceCreateInfo.free();
		deviceQueueCreateInfo.free();
		
		memFree(queuePriorities);
		memFree(ppEnabledLayerNames);
		
		memFree(pdev);
		
		return out;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates VkInsatnce.</p>
	 * @param appInfo				- Must be valid VkApplicationInfo.
	 * @param validationLayers		- Validation layers that should be enabled.
	 * @param requiredExtensions	- Required by GLFW Vulkan extensions.
	 * @param userExtensions		- Optional extensions.
	 * @return		<p>VkInstance</p>
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 * @see {@link org.lwjgl.vulkan.VkApplicationInfo}
	 */
	public static VkInstance createInstance(VkApplicationInfo appInfo, ByteBuffer[] validationLayers, PointerBuffer requiredExtensions, ByteBuffer... userExtensions) {		
		PointerBuffer ppEnabledLayerNames = memAllocPointer(validationLayers.length);
		for(int i = 0; HardwareManager.validation && i < validationLayers.length; i++)
			ppEnabledLayerNames.put(validationLayers[i]);
		ppEnabledLayerNames.flip();
		
		PointerBuffer extensions = memAllocPointer(requiredExtensions.remaining() + userExtensions.length);
		extensions.put(requiredExtensions);
		for(int i = 0; i < userExtensions.length; i++)
			extensions.put(userExtensions[i]);
		extensions.flip();
		
		VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
				.pNext(NULL)
				.pApplicationInfo(appInfo)
				.ppEnabledExtensionNames(extensions)
				.ppEnabledLayerNames(ppEnabledLayerNames);
		
		PointerBuffer inst = memAllocPointer(1);
		int err = vkCreateInstance(instanceCreateInfo, null, inst);
		if(err != 0)
			throw new AssertionError("Could not create VkInstance. " + translateVulkanError(err));
		long instanceAdr = inst.get(0);
		VkInstance out = new VkInstance(instanceAdr, instanceCreateInfo);
		
		memFree(inst);
		memFree(extensions);
		memFree(ppEnabledLayerNames);
		
		return out;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Frees contents of buffers.</p>
	 * @param buffers	- Buffers to free.
	 */
	public static void freeBuffers(Buffer...buffers) {
		for(Buffer bb : buffers)
			memFree(bb);
	}
	
	static final String[] errorMessages = {
					//Success codes
						"Command successfully completed", 
						"A fence or query has not yet completed", 
						"A wait operation has not completed in the specified time", 
						"An event is signaled",
						"An event is unsignaled",
						"A return array was too small for the result",
						
					//Error codes
						"A host memory allocation has failed.",
						"A device memory allocation has failed.",
						"Initialization of an object could not be completed for implementation-specific reasons.",
						"The logical or physical device has been lost.",
						"Mapping of a memory object has failed.",
						"A requested layer is not present or could not be loaded.",
						"A requested extension is not supported.",
						"A requested feature is not supported.",
						"The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons.",
						"Too many objects of the type have already been created.",
						"A requested format is not supported on this device.",
						"A pool allocation has failed due to fragmentation of the pool’s memory. This must only be returned if no attempt to allocate host or device memory was made to accomodate the new allocation."
						};
	/**
	 * <h5>Description:</h5>
	 * <p>Translates Vulkan error code to text.</p>
	 * @param err	- Vulkan error code.
	 * @return Translated error description.
	 */
	public static String translateVulkanError(int err) {
		//Success codes:
		if(err >= 0)
			return errorMessages[err];
		//Error codes:
		return errorMessages[5 - err];
	}
}
