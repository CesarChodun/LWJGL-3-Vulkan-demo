package core.employees;

import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkDevice;

/**
 * <h5>Description:</h5>
 * <p>Prototype class for managing <code><b><i>VkDevice</i></b></code> capabilities.</p>
 * @author cezarychodun
 *	@see {@link org.lwjgl.vulkan.VkDevice}
 */
public class LogicalDevice extends VkDevice{
	
	private int graphicsQueueFamilyIndex;

	public LogicalDevice(long handle, VkPhysicalDevice physicalDevice, VkDeviceCreateInfo ci) {
		super(handle, physicalDevice, ci);
	}
	
	public int getGraphicsQueueFamilyIndex() {
		return graphicsQueueFamilyIndex;
	}
	public void setGraphicsQueueFamilyIndex(int graphicsQueueFamilyIndex) {
		this.graphicsQueueFamilyIndex = graphicsQueueFamilyIndex;
	}
}
