package core.employees;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static java.lang.Math.*;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

/**
 *	<h5>Description: </h5>
 *		<p>This class provide simplified access to children of <code>VkPhysicalDevice</code>.</p>
 *
 * 	@author Cezary Choduń
 *	@see {@link org.lwjgl.vulkan.VkPhysicalDevice}
 */
public class PhysicalDevice extends VkPhysicalDevice{
	
	public VkPhysicalDeviceProperties properties;
	public VkQueueFamilyProperties.Buffer queueFamilyProperties;
	public int queueFamilyCount;

	/**
	 * <h5>Description</h5>
	 * 
	 * <p>This constructor creates new <code>VkPhysicalDevice</code> and acquire informations about it.
	 * I.e <code>VkPhysicalDeviceProperties</code>, <code>VkQueueFamilyProperties</code></p>
	 * 
	 * @param handle 	- As specified in <code><b>VkPhysicalDevice</b></code>
	 * @param instance	- As specified in <code><b>VkPhysicalDevice</b></code>
	 * 
	 * @see {@link org.lwjgl.vulkan.VkPhysicalDevice}
	 */
	public PhysicalDevice(long handle, VkInstance instance) {
		super(handle, instance);
		
		acquireProperties(instance);
	}
	
	/**
	 * <h5>Description</h5>
	 * 
	 * <p>Function for acquiring queue family index which meets requirements(<code><i><b>VkQueueFlagBits</b></i></code>).</p>
	 * 
	 * @param first 			- First interesting queue index. By default <b>0</b>.
	 * @param requiredSupport 	- Required queue flag bits(<code><i><b>VkQueueFlagBits</b></i></code>). For example <code>VK_QUEUE_GRAPHICS_BIT</code>.<br>
	 * 							If <code><b><i>requiredExtensions</i></b></code> equals to <b>0</b> and is less than <code><b><i>queueFamilyCount</i></b></code> 
	 * 							next queue family will be returned.
	 * @return	If queue family has been found, the index of such family is returned. Otherwise <b>-1</b> is returned.
	 * @see {@link org.lwjgl.vulkan.VK10#VK_QUEUE_GRAPHICS_BIT}
	 */
	public int getNextQueueFamilyIndex(int first, int requiredSupport) {
		for(int i = max(0, first); i < queueFamilyCount; i++)
			if((queueFamilyProperties.get(i).queueFlags() & requiredSupport) == requiredSupport)
				return i;
		return -1;
	}
	
	/**
	 * <h5>Description</h5>
	 * <p>Acquire device and queue properties.</p>
	 * @param instance - <b>Must</b> be a valid <code>VkInstance</code>.
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 */
	protected void acquireProperties(VkInstance instance) {
		properties = VkPhysicalDeviceProperties.calloc();
		vkGetPhysicalDeviceProperties(this, properties);
		
		IntBuffer queue_family_count_buffer = memAllocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(this, queue_family_count_buffer, null);
		queueFamilyCount = queue_family_count_buffer.get(0);
		
		queueFamilyProperties = VkQueueFamilyProperties.calloc(queueFamilyCount);
		vkGetPhysicalDeviceQueueFamilyProperties(this, queue_family_count_buffer, queueFamilyProperties);
		memFree(queue_family_count_buffer);
	}
	
	/**
	 * <h5>Description</h5>
	 * <p>Frees information acquired by <code>PhysicalDevice</code> but leaves <code>VkPhysicalDevice</code> intact.</p>
	 * @see {@link org.lwjgl.vulkan.VkPhysicalDevice}
	 */
	protected void freeProperties() {
		queueFamilyProperties.free();
		properties.free();
	}
	
	/**
	 * <h5>Description</h5>
	 * <p>Frees information acquired by <code><b><i>PhysicalDevice</i></b></code>.</p>
	 * <h5>Note</h5>
	 * <p>Please note that despite of this call <code><b><i>VkPhysicalDevice</i></b></code> remains intact, and <b>may</b> be still used.</p>
	 * @see {@link org.lwjgl.vulkan.VkPhysicalDevice}
	 */
	public void destroyProperties() {
		freeProperties();
	}
}
