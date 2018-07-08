package core.employees;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import core.managers.Util;

/**
 * 
 * @author Cezary Choduń
 *
 */
public class Swapchain {

	public long swapchainHandle = VK_NULL_HANDLE;
    public long[] images;
    public long[] imageViews;
    
    private int width, height;
    private ColorFormatAndSpace colorFormatAndSpace;
    
    
    
    
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Gets resolution range.
	 * </p>
     * @param capabilities - Surface capabilities.
     * @return
     */
    private static int[] getSwapchainResolutionRange(VkSurfaceCapabilitiesKHR capabilities) {
    	int[] out = new int[4];
    	
    	out[0] = capabilities.minImageExtent().width();
    	out[1] = capabilities.minImageExtent().height();
    	out[2] = capabilities.maxImageExtent().width();
    	out[3] = capabilities.maxImageExtent().height();
    	
    	return out;
    }
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Checks if the resolution is acceptable.
	 * </p>
     * @param width		- Width to check.
     * @param height	- Height to check.
     * @param range		- Acceptable range.
     * @return
     */
    private static boolean checkResolution(int width, int height, int[] range) {
    	return !(width < range[0] || width > range[2] || height < range[1] || height > range[2]);
    }
	
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Gets current surface present modes.
	 * </p>
     * @param physicalDevice
     * @param surface
     * @return
     */
    public static IntBuffer getSurfacePresentModes(VkPhysicalDevice physicalDevice, long surface) {
    	IntBuffer pCount = memAllocInt(1);
    	int err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, null);
    	if(err != VK_SUCCESS)
    		throw new AssertionError("Failed to obtain present mode count: " + Util.translateVulkanError(err));
    	int count = pCount.get(0);
    	
    	IntBuffer modes = memAllocInt(count);
    	err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, modes);
    	if(err != VK_SUCCESS)
    		throw new AssertionError("Failed to obtain surface present modes: " + Util.translateVulkanError(err));

    	memFree(pCount);
    	return modes;
    }
    
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Outputs best supported surface present mode.
	 * </p>
     * @param physicalDevice
     * @param surface
     * @param presentModeHierarchy
     * @return
     */
    private static int getBestPresentMode(VkPhysicalDevice physicalDevice, long surface, int[] presentModeHierarchy) {
    	IntBuffer modes = getSurfacePresentModes(physicalDevice, surface);
    	int bsize = modes.remaining();
    	int hsize = presentModeHierarchy.length;
    	
    	for(int i = 0; i < hsize; i++)
    		for(int j = 0; j < bsize; j++)
    			if(modes.get(j) == presentModeHierarchy[i])
    				return modes.get(j);
    	
    	memFree(modes);
    	
    	return -1;
    }
    
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Recreates swapchain, or creates new one if no swapchain was created earlier.
	 * 		<b>Note</b> when creating new swapchain image views from the old one are deleted automatically.
	 * </p>
     * @param device
     * @param physicalDevice
     * @param surface
     * @param width
     * @param height
     * @param presentModeHierarchy
     * @param colorFormatAndSpace
     */
    public void recreateSwapchain(VkDevice device, VkPhysicalDevice physicalDevice, long surface, int width, int height, int[] presentModeHierarchy, ColorFormatAndSpace colorFormatAndSpace, int graphicsQueueFamilyIndex, int presentQueueFamilyIndex) {
    	this.width = width;
    	this.height = height;
    	this.colorFormatAndSpace = colorFormatAndSpace;
    	
    	VkSurfaceCapabilitiesKHR caps = Util.getSurfaceCapabilities(physicalDevice, surface);
    	long oldSwapchainHandle = swapchainHandle;
    	
    	if(!checkResolution(this.width, this.height, getSwapchainResolutionRange(caps))) {
    		int[] range = getSwapchainResolutionRange(caps);
    		throw new IllegalStateException("Swapchain width and/or height are outside surface specific range. Current size: width = "
    				+ this.width + ", height = " + this.height + ". While the range is: width e <" + range[0] + "; " + range[2] + ">,"
    				+ "height e <" + range[1] + "; " + range[3] +  ">.");
    	}
    	
    	//Swapchain presentation mode:
    	int swapchainPresentMode = getBestPresentMode(physicalDevice, surface, presentModeHierarchy);
    	if(swapchainPresentMode == -1)
    		throw new AssertionError("Failed to locate any suitable mode!");
    	//Triple buffering:
    	int imageCount = caps.minImageCount() + 1;
    	if(caps.maxImageCount() > 0 && imageCount > caps.maxImageCount())
    		imageCount = caps.maxImageCount();
    	//Swap extent
    	VkExtent2D extent = VkExtent2D.calloc()
    			.width(width)
    			.height(height);
    	//Transform
    	int transform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
    	if((caps.supportedTransforms() & transform) == 0)
    		transform = caps.currentTransform();
    	
    	//Create info for new swapchain
    	VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc()
    			.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
    			.pNext(NULL)
    			.surface(surface)
    			.minImageCount(imageCount)
    			.imageFormat(colorFormatAndSpace.colorFormat)
    			.imageColorSpace(colorFormatAndSpace.colorSpace)
    			.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
    			.preTransform(transform)
    			.imageArrayLayers(1)
    			.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
    			.pQueueFamilyIndices(null)
    			.presentMode(swapchainPresentMode)
    			.oldSwapchain(oldSwapchainHandle)
    			.clipped(true)
    			.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
    			.imageExtent(extent);
    	if(graphicsQueueFamilyIndex != presentQueueFamilyIndex) {//TODO: Check if valid
    		IntBuffer buf = memAllocInt(2);
    		buf.put(graphicsQueueFamilyIndex);
    		buf.put(presentQueueFamilyIndex);
    		buf.flip();
    		createInfo.pQueueFamilyIndices(buf);
    		createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
    		memFree(buf);
    	}
    			
    	LongBuffer pSwapchain = memAllocLong(1);
    	int err = vkCreateSwapchainKHR(device, createInfo, null, pSwapchain);
    	if(err != VK_SUCCESS)
    		throw new AssertionError("Failed to recreate swapchain: " + Util.translateVulkanError(err));
    	swapchainHandle = pSwapchain.get(0);
    	
    	//Destroying old swapchain.
    	if(oldSwapchainHandle != VK_NULL_HANDLE)
    		vkDestroySwapchainKHR(device, oldSwapchainHandle, null);
    	
    	//Extracting image handles from swapchain.
    	IntBuffer pSwapchainImageCount = memAllocInt(1);
    	err = vkGetSwapchainImagesKHR(device, swapchainHandle, pSwapchainImageCount, null);
    	if(err != VK_SUCCESS)
    		throw new AssertionError("Failed to enumerate swapchain images: " + Util.translateVulkanError(err));
    	
    	int swapchainImageCount = pSwapchainImageCount.get(0);
    	
    	LongBuffer pSwapchainImages = memAllocLong(swapchainImageCount);
    	err = vkGetSwapchainImagesKHR(device, swapchainHandle, pSwapchainImageCount, pSwapchainImages);
    	if(err != VK_SUCCESS)
    		throw new AssertionError("Failed to obtain swapchain images: " + Util.translateVulkanError(err));
    	
    	images = new long[swapchainImageCount];
    	for(int i = 0; i < swapchainImageCount; i++)
    		images[i] = pSwapchainImages.get(i);
    	
    	//Clean up
    	caps.free();
    	memFree(pSwapchain);
    	memFree(pSwapchainImageCount);
    	memFree(pSwapchainImages);
    }
    
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Creates image views(swapchain <b>must</b> be created before this call).
	 * </p>
     * @param device
     * @param colorAttachmentView
     */
    public void createImageViews(VkDevice device, VkImageViewCreateInfo colorAttachmentView) {
    	if(imageViews != null)
    		destroyImageViews(device);
    	
    	imageViews = new long[images.length];
    	
    	LongBuffer pView = memAllocLong(1);
    	for(int i = 0; i < images.length; i++) {
    		colorAttachmentView.image(images[i]);
    		int err = vkCreateImageView(device, colorAttachmentView, null, pView);
    		if(err != VK_SUCCESS)
    			throw new AssertionError("Failed to create image view: " + Util.translateVulkanError(err));
    		
    		imageViews[i] = pView.get(0);
    	}
    	
    	memFree(pView);
    }
    
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Destroys swapchain image views.
	 * 		<b>Note</b> when creating new swapchain image views from the old one are deleted automatically.
	 * </p>
     * @param device
     */
    public void destroyImageViews(VkDevice device) {    	
    	for(int i = 0; i < imageViews.length; i++)
    		vkDestroyImageView(device, imageViews[i], null);
    }
    
    
    
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public ColorFormatAndSpace getColorFormatAndSpace() {
		return colorFormatAndSpace;
	}
}
