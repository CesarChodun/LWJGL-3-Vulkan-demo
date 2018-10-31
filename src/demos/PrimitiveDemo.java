package demos;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.eclipse.jdt.annotation.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.*;

import core.employees.ColorFormatAndSpace;
import core.employees.FPSCounter;
import core.employees.LogicalDevice;
import core.employees.PhysicalDevice;
import core.employees.Swapchain;
import core.employees.Window;
import core.managers.EngineVersioning;
import core.managers.HardwareManager;
import core.managers.Util;

/**
 * <h5>Description:</h5>
 * <p>Class for testing vulkan/moltenVK installation and setup.</p>
 * @author Cezary Choduń
 *
 */
public class PrimitiveDemo {
	
	 /**
     * This is just -1L, but it is nicer as a symbolic constant.
     */
    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
	
	private static Window window;
	private static PhysicalDevice physicalDevice;
	private static LogicalDevice logicalDevice;	 
	private static int graphicsQueueFamilyIndex = -1;

	static int width = 800;
	static int height = 600;
	
	static boolean quad = true;
	
	static final float rb = 0.9f, gb = 0.9f, bb = 0.9f;
	 
	 
	 private static class Vertices {
		 long verticesBuf;
		 VkPipelineVertexInputStateCreateInfo createInfo;
	 }
	 
	 private static Vertices createVertices(VkPhysicalDeviceMemoryProperties logicalDeviceMemoryProperties, VkDevice logicalDevice) {
		 ByteBuffer vertexBuffer;
		 if(quad)
			 vertexBuffer = memAlloc(6*2*4);
		 else
			 vertexBuffer = memAlloc(3*2*4);
		 FloatBuffer fb = vertexBuffer.asFloatBuffer();
		 
		 
		 if(quad) {
			 fb.put(-0.5f).put(-0.5f);
			 fb.put(0.5f).put(-0.5f);
			 fb.put(-0.5f).put(0.5f);
			 fb.put(-0.5f).put(0.5f);
			 fb.put(0.5f).put(-0.5f);
			 fb.put(0.5f).put(0.5f);
		 }
		 else {
			 fb.put(-0.5f).put(-0.5f);
			 fb.put( 0.5f).put(-0.5f);
			 fb.put( 0.0f).put( 0.5f);
		 }
		 
		 VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				 .pNext(NULL)
				 .allocationSize(0)
				 .memoryTypeIndex(0);
		 VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		 
		 VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				 .pNext(NULL)
				 .size(vertexBuffer.remaining())
				 .flags(0);
		 
		 LongBuffer pBuffer = memAllocLong(1);
		 int err = vkCreateBuffer(logicalDevice, bufInfo, null, pBuffer);
		 if(err != VK_SUCCESS)
			 throw new AssertionError("Failed to create buffer: " + Util.translateVulkanError(err));
		 long verticesBuffer = pBuffer.get(0);
		 memFree(pBuffer);
		 bufInfo.free();
		 
		 vkGetBufferMemoryRequirements(logicalDevice, verticesBuffer, memReqs);
		 memAlloc.allocationSize(memReqs.size());
		 IntBuffer memoryTypeIndex = memAllocInt(1);
		 if(!Util.getMemoryType(logicalDeviceMemoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex))
			 throw new AssertionError("Failed to obtain memry type.");
		 memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		 memFree(memoryTypeIndex);
		 memReqs.free();
		 
		 LongBuffer pMemory = memAllocLong(1);
		 err = vkAllocateMemory(logicalDevice, memAlloc, null, pMemory);
		 long memory = pMemory.get(0);
		 if(err != VK_SUCCESS)
			 throw new AssertionError("Failed to alocate vertex memory: " + Util.translateVulkanError(err));
		 
		 PointerBuffer pData = memAllocPointer(1);
		 err = vkMapMemory(logicalDevice, memory, 0, memAlloc.allocationSize(), 0, pData);
		 memAlloc.free();
		 long data = pData.get(0);
		 memFree(pData);
		 if(err != VK_SUCCESS)
			 throw new AssertionError("Failed to map memory: " + Util.translateVulkanError(err));
		 
		 MemoryUtil.memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
		 memFree(vertexBuffer);
		 vkUnmapMemory(logicalDevice, memory);
		 err = vkBindBufferMemory(logicalDevice, verticesBuffer, memory, 0);
		 if(err != VK_SUCCESS)
			 throw new AssertionError("Failed to bind memory to vertex buffer: " + Util.translateVulkanError(err));
		 
		 //Binding:
		 VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1)
				 .binding(0) // <- we bind our vertex buffer to point 0
				 .stride(2*4)
				 .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		 
		 // Attribute descriptions
		 // Describes memory layout and shader attribute locations
		 VkVertexInputAttributeDescription.Buffer attributeDescription = VkVertexInputAttributeDescription.calloc(1);
		 //Location 0 : Position
		 attributeDescription.get(0)
		 		.binding(0)
		 		.location(0)
		 		.format(VK_FORMAT_R32G32_SFLOAT)
		 		.offset(0);
		 
		 //asign to vertex buffer
		 VkPipelineVertexInputStateCreateInfo vertexCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
				 .pNext(NULL)
				 .pVertexBindingDescriptions(bindingDescription)
				 .pVertexAttributeDescriptions(attributeDescription);
		 
		 Vertices ret = new Vertices();
		 ret.createInfo = vertexCreateInfo;
		 ret.verticesBuf = verticesBuffer;
		 
		 return ret;
	 }
	 
	 private static long createPipeline(VkDevice logicalDevice, long renderPass, VkPipelineVertexInputStateCreateInfo vertexCreateInfo) {
		 // Vertex input state
	     // Describes the topoloy used with this pipeline
		 VkPipelineInputAssemblyStateCreateInfo inputAsemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc()
		 	.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
		 	.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		 
		 //Rasterization state
		 VkPipelineRasterizationStateCreateInfo rasterizationStateInfo = VkPipelineRasterizationStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
				 .polygonMode(VK_POLYGON_MODE_FILL)
				 .cullMode(VK_CULL_MODE_NONE)
				 .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
				 .depthClampEnable(false)
				 .rasterizerDiscardEnable(false)
				 .depthBiasEnable(false);
		 
		 // Color blend state
		 // Describes blend modes and color masks
		 VkPipelineColorBlendAttachmentState.Buffer colorWriteMask = VkPipelineColorBlendAttachmentState.calloc(1)
				 .blendEnable(false)
				 .colorWriteMask(0xF);
		 VkPipelineColorBlendStateCreateInfo colorBlendStateInfo = VkPipelineColorBlendStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
				 .pAttachments(colorWriteMask);
		 
		 // Viewport state
		 VkPipelineViewportStateCreateInfo viewportStateCreateInfo = VkPipelineViewportStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
				 .viewportCount(1) // <- one viewport
				 .scissorCount(1); // <- one scissor rectangle
		 
		// Enable dynamic states
	        // Describes the dynamic states to be used with this pipeline
	        // Dynamic states can be set even after the pipeline has been created
	        // So there is no need to create new pipelines just for changing
	        // a viewport's dimensions or a scissor box
		 IntBuffer dynamicStates = memAllocInt(2);
		 dynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip();
		 VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
				 // The dynamic state properties themselves are stored in the command buffer
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
				 .pDynamicStates(dynamicStates);
		 
		 //Depth and stencil state
	     // Describes depth and stenctil test and compare ops
		 VkPipelineDepthStencilStateCreateInfo depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
				 // No depth test/write and no stencil used 
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
				 .depthTestEnable(false)
				 .depthWriteEnable(false)
				 .depthCompareOp(VK_COMPARE_OP_ALWAYS)
				 .depthBoundsTestEnable(false)
				 .stencilTestEnable(false);
		 depthStencilState.back()
		 .failOp(VK_STENCIL_OP_KEEP)
		 .passOp(VK_STENCIL_OP_KEEP)
		 .compareOp(VK_COMPARE_OP_ALWAYS);
		 depthStencilState.front(depthStencilState.back());
		 
		 //Multi sampling state
		// No multi sampling used in this example
		 VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
				 .pSampleMask(null)
				 .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
		 
		 //Load shaders
		 VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(2);
		 stages.get(0).set(Util.createShaderStage(Util.createShaderModule(logicalDevice, new File("storage/res/shaders/triangle.vert.spv")), VK_SHADER_STAGE_VERTEX_BIT, "main"));
		 stages.get(1).set(Util.createShaderStage(Util.createShaderModule(logicalDevice, new File("storage/res/shaders/triangle.frag.spv")), VK_SHADER_STAGE_FRAGMENT_BIT, "main"));
		 
		 // Create the pipeline layout that is used to generate the rendering pipelines that
	     // are based on this descriptor set layout
		 VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
				 .pNext(NULL)
				 .pSetLayouts(null);
		 
		 LongBuffer pLayout = memAllocLong(1);
		 int err = vkCreatePipelineLayout(logicalDevice, pipelineLayoutCreateInfo, null, pLayout);
		 if(err != VK_SUCCESS)
			 throw new AssertionError("Failed to create pipeline layout: " + Util.translateVulkanError(err));
		 long layout = pLayout.get(0);
		 memFree(pLayout);
		 pipelineLayoutCreateInfo.free();
		 
		 VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
				 .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
				 .layout(layout)	// <- the layout used for this pipeline (NEEDS TO BE SET! even though it is basically empty)
				 .renderPass(renderPass)// <- renderpass this pipeline is attached to
				 .pVertexInputState(vertexCreateInfo)
				 .pInputAssemblyState(inputAsemblyInfo)
				 .pRasterizationState(rasterizationStateInfo)
				 .pColorBlendState(colorBlendStateInfo)
				 .pMultisampleState(multisampleState)
				 .pViewportState(viewportStateCreateInfo)
				 .pDepthStencilState(depthStencilState)
				 .pStages(stages)
				 .pDynamicState(dynamicState);
		 
		 LongBuffer pPipeline = memAllocLong(1);
		 err = vkCreateGraphicsPipelines(logicalDevice, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipeline);
		 long pipelineHandle = pPipeline.get(0);
		 memFree(pPipeline);
		 inputAsemblyInfo.free();
		 rasterizationStateInfo.free();
		 colorWriteMask.free();
		 colorBlendStateInfo.free();
		 viewportStateCreateInfo.free();
		 memFree(dynamicStates);
		 dynamicState.free();
		 depthStencilState.free();
		 multisampleState.free();
		 stages.free();
		 pipelineCreateInfo.free();
		 
		 return pipelineHandle;
	 }
	 
	//Util:		
		public static long createRenderPass(VkDevice logicalDevice, int colorFormat) {
			VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1)
					.format(colorFormat)
					.samples(VK_SAMPLE_COUNT_1_BIT)
					.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
					.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
					.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
					.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
					.initialLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
					.attachment(0)
					.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
			
			VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1)
					.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
					.flags(0)
					.pInputAttachments(null)
					.colorAttachmentCount(colorReference.remaining())
					.pColorAttachments(colorReference)
					.pResolveAttachments(null)
					.pDepthStencilAttachment(null)
					.pPreserveAttachments(null);
			
			VkRenderPassCreateInfo pCreateInfo = VkRenderPassCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
					.pNext(NULL)
					.pAttachments(attachments)
					.pSubpasses(subpass)
					.pDependencies(null);
			
			LongBuffer pRenderPass = memAllocLong(1);
			int err = vkCreateRenderPass(logicalDevice, pCreateInfo, null, pRenderPass);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to create render pass: " + Util.translateVulkanError(err));
			
			long handle = pRenderPass.get(0);
			
			memFree(pRenderPass);
			attachments.free();
			colorReference.free();
			subpass.free();
			pCreateInfo.free();
			
			return handle;
		}
		
		private static void imageBarrier(VkCommandBuffer cmdBuffer, long image, int aspectMask, int oldImageLayout, int srcAccess, int newImageLayout, int dstAccess) {
			
			VkImageMemoryBarrier.Buffer imageMemoryBarrier = VkImageMemoryBarrier.calloc(1)
					.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
					.pNext(NULL)
					.oldLayout(oldImageLayout)
					.srcAccessMask(srcAccess)
					.newLayout(newImageLayout)
					.dstAccessMask(dstAccess)
					.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.image(image);
			imageMemoryBarrier.subresourceRange()
				.aspectMask(aspectMask)
				.baseMipLevel(0)
				.levelCount(1)
				.layerCount(1);
			
			// Put barrier on top
			int srcStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
			int destStageFlags = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
			
			// Put barrier inside setup command buffer
			vkCmdPipelineBarrier(cmdBuffer, srcStageFlags, destStageFlags, 0, null, null, imageMemoryBarrier);
			imageMemoryBarrier.free();
		}
		
		public static Swapchain createSwapchain(VkDevice logicalDevice, VkPhysicalDevice physicalDevice, long surface, @Nullable Swapchain swapchain, VkCommandBuffer commandBuffer, int newWidth, int newHeight, ColorFormatAndSpace colorFormatAndSpace) {
			if(swapchain == null)
				swapchain = new Swapchain();
			
			int[] presentModeHierarchy = new int[3];
			presentModeHierarchy[0] = VK_PRESENT_MODE_MAILBOX_KHR;
			presentModeHierarchy[1] = VK_PRESENT_MODE_IMMEDIATE_KHR;
			presentModeHierarchy[2] = VK_PRESENT_MODE_FIFO_KHR;
			
			swapchain.recreateSwapchain(logicalDevice, physicalDevice, surface, width, height, presentModeHierarchy, colorFormatAndSpace, ((LogicalDevice) logicalDevice).getGraphicsQueueFamilyIndex(), ((LogicalDevice) logicalDevice).getGraphicsQueueFamilyIndex());
			
			for(int i = 0; i < swapchain.images.length; i++) 
				imageBarrier(commandBuffer, swapchain.images[i], VK_IMAGE_ASPECT_COLOR_BIT, VK_IMAGE_LAYOUT_UNDEFINED, 0, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
			
			VkImageViewCreateInfo colorAttachmentView = VkImageViewCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
				.pNext(NULL)
				.format(colorFormatAndSpace.colorFormat)
				.viewType(VK_IMAGE_VIEW_TYPE_2D)
				.flags(0);
			colorAttachmentView.components()
				.r(VK_COMPONENT_SWIZZLE_R)
				.g(VK_COMPONENT_SWIZZLE_G)
				.b(VK_COMPONENT_SWIZZLE_B)
				.a(VK_COMPONENT_SWIZZLE_A);
			colorAttachmentView.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
			swapchain.createImageViews(logicalDevice, colorAttachmentView);
			
			return swapchain;
		}
		
		private static void submitCommandBuffer(VkQueue queue, VkCommandBuffer commandBuffer) {
			if(commandBuffer == null || commandBuffer.address() == NULL)
				return;
			
			VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
			PointerBuffer pCommandBuffer = memAllocPointer(1)
					.put(commandBuffer)
					.flip();
			submitInfo.pCommandBuffers(pCommandBuffer);
			int err = vkQueueSubmit(queue, submitInfo, 0);
			memFree(pCommandBuffer);
			submitInfo.free();
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to submit command buffer: " + Util.translateVulkanError(err));
		}
		
		private static long[] createFramebuffers(VkDevice logicalDevice, Swapchain swapchain, long renderPass) {
			
			LongBuffer attachments = memAllocLong(1);
			VkFramebufferCreateInfo fci = VkFramebufferCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
					.pNext(NULL)
					.pAttachments(attachments)
					.flags(0)
					.height(height)
					.width(width)
					.layers(1)
					.renderPass(renderPass);
			
			//Create framebuffer for each swapchain image
			long[] framebuffers = new long[swapchain.images.length];
			LongBuffer pFramebuffer = memAllocLong(1);
			for(int  i = 0; i < swapchain.images.length; i++) {
				attachments.put(0, swapchain.imageViews[i]);
				int err = vkCreateFramebuffer(logicalDevice, fci, null, pFramebuffer);
				long framebuffer = pFramebuffer.get(0);
				if(err != VK_SUCCESS)
					throw new AssertionError("Failed to create framebuffer: " + Util.translateVulkanError(err));
				
				framebuffers[i] = framebuffer;
			}
			
			memFree(attachments);
			memFree(pFramebuffer);
			fci.free();
			
			return framebuffers;
		}
		
		private static VkImageMemoryBarrier.Buffer createPrePresentBarrier(long presentImage){
			VkImageMemoryBarrier.Buffer out = VkImageMemoryBarrier.calloc(1)
					.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
					.pNext(NULL)
					.srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
					.dstAccessMask(0)
					.oldLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
					.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			out.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
			out.image(presentImage);
			return out;
		}
		
		private static VkCommandBuffer[] createRenderCommandBuffers(VkDevice logicalDevice, long commandPool, Swapchain swapchain, long[] framebuffers, long renderPass, long pipeline, long verticesBuf) {
			// Create the render command buffers (one command buffer per framebuffer image)
			VkCommandBufferAllocateInfo cmdBufAllocateInfo = VkCommandBufferAllocateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
					.pNext(NULL)
					.commandPool(commandPool)
					.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
					.commandBufferCount(framebuffers.length);
			
			PointerBuffer pCommandBuffer = memAllocPointer(framebuffers.length);
			int err = vkAllocateCommandBuffers(logicalDevice, cmdBufAllocateInfo, pCommandBuffer);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to allocate command buffers: " + Util.translateVulkanError(err));
			
			VkCommandBuffer[] renderCommandBuffers = new VkCommandBuffer[framebuffers.length];
			for(int i = 0; i < framebuffers.length; i++)
				renderCommandBuffers[i] = new VkCommandBuffer(pCommandBuffer.get(i), logicalDevice);
			memFree(pCommandBuffer);
			cmdBufAllocateInfo.free();
			
			// Create the command buffer begin structure
			VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
					.pNext(NULL);
			
			//Specify clear color.
			VkClearValue.Buffer clearValues = VkClearValue.calloc(1);
			clearValues.color()
				.float32(0, rb)
	            .float32(1, gb)
	            .float32(2, bb)
	            .float32(3, 1.0f);
			
			// Specify everything to begin a render pass
			VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
					.pNext(NULL)
					.renderPass(renderPass)
					.pClearValues(clearValues);
			VkRect2D renderArea = renderPassBeginInfo.renderArea();
			renderArea.offset().set(0, 0);
			renderArea.extent().set(width, height);
			
			for(int i = 0; i < framebuffers.length; i++) {
				// Set target frame buffer
				renderPassBeginInfo.framebuffer(framebuffers[i]);
				
				err = vkBeginCommandBuffer(renderCommandBuffers[i], cmdBufInfo);
				if(err != VK_SUCCESS)
					throw new AssertionError("Failed to begin render command buffer: " + Util.translateVulkanError(err));
				
				vkCmdBeginRenderPass(renderCommandBuffers[i], renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
				
				// Update dynamic viewport state
				VkViewport.Buffer viewport = VkViewport.calloc(1)
						.height(height)
						.width(width)
						.minDepth(0.0f)
						.maxDepth(1.0f);
				vkCmdSetViewport(renderCommandBuffers[i], 0, viewport);
				viewport.free();
				
				//Update dynamic scissor state
				VkRect2D.Buffer scissor = VkRect2D.calloc(1);
				scissor.extent().set(width, height);
				scissor.offset().set(0, 0);
				vkCmdSetScissor(renderCommandBuffers[i], 0, scissor);
				scissor.free();
				
				//Bind the rendering pipeline (including the shaders)
				vkCmdBindPipeline(renderCommandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
				
				// Bind triangle vertices
				LongBuffer offsets = memAllocLong(1);
				offsets.put(0, 0L);
				LongBuffer pBuffers = memAllocLong(1);
				pBuffers.put(0, verticesBuf);
				vkCmdBindVertexBuffers(renderCommandBuffers[i], 0, pBuffers, offsets);
				
				memFree(pBuffers);
				memFree(offsets);
				
				//Draw triangle
				if(!quad)
					vkCmdDraw(renderCommandBuffers[i], 3, 1, 0, 0);
				else
					vkCmdDraw(renderCommandBuffers[i], 6, 1, 0, 0);
				
				vkCmdEndRenderPass(renderCommandBuffers[i]);
				
				// Add a present memory barrier to the end of the command buffer
	            // This will transform the frame buffer color attachment to a
	            // new layout for presenting it to the windowing system integration
				VkImageMemoryBarrier.Buffer prePresentBarrier = createPrePresentBarrier(swapchain.images[i]);
				vkCmdPipelineBarrier(renderCommandBuffers[i],
						VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT, 
						VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 
						0,
						null,
						null,
						prePresentBarrier);
				prePresentBarrier.free();
				
				err = vkEndCommandBuffer(renderCommandBuffers[i]);
				if(err != VK_SUCCESS)
					throw new AssertionError("Failed to end render command buffer" + Util.translateVulkanError(err));
			}
			
			renderPassBeginInfo.free();
			clearValues.free();
			cmdBufInfo.free();
			
			return renderCommandBuffers;
		}
		
		private static VkImageMemoryBarrier.Buffer createPostPresentBarrier(long presentImage){
			VkImageMemoryBarrier.Buffer out = VkImageMemoryBarrier.calloc(1)
				.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
				.pNext(NULL)
				.srcAccessMask(0)
				.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
				.oldLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
				.newLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
				.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
				.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
			out.subresourceRange()
				.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
				.baseMipLevel(0)
				.levelCount(1)
				.baseArrayLayer(0)
				.layerCount(1);
			out.image(presentImage);
			return out;
		}
		
		private static void submitPostPresentBarrier(long image, VkCommandBuffer commandBuffer, VkQueue queue) {
			VkCommandBufferBeginInfo begininfo = VkCommandBufferBeginInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
					.pNext(NULL);
			
			int err = vkBeginCommandBuffer(commandBuffer, begininfo);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to begin command buffer: " + Util.translateVulkanError(err));
			
			VkImageMemoryBarrier.Buffer postPresentBarrier = createPostPresentBarrier(image);
			vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, 0, null, null, postPresentBarrier);
			postPresentBarrier.free();
			
			err = vkEndCommandBuffer(commandBuffer);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to end command buffer: " + Util.translateVulkanError(err));
			
			submitCommandBuffer(queue, commandBuffer);
		}

		static Swapchain swapchain = null;
		static long[] framebuffers = null;
		static VkCommandBuffer[] renderCommandBuffers;

		static class SwapchainRecreator {
			static boolean mustRecreate = true;
			
			public static void recreate(VkCommandBuffer setupCommandBuffer, VkDevice logicalDevice, VkQueue queue, 
					long surface, long renderPass, long renderCommandPool, 
					ColorFormatAndSpace color, long pipeline, long verticesBuf) {
				VkCommandBufferBeginInfo cmdBufInfo = VkCommandBufferBeginInfo.calloc()
						.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
						.pNext(NULL);
				int err = vkBeginCommandBuffer(setupCommandBuffer, cmdBufInfo);
				cmdBufInfo.free();
				if(err != VK_SUCCESS)
					throw new AssertionError("Failed to begin setup command buffer: " + Util.translateVulkanError(err));
				
				swapchain = createSwapchain(logicalDevice, physicalDevice, surface, swapchain, setupCommandBuffer, width, height, 
						color);
				err = vkEndCommandBuffer(setupCommandBuffer);
				if(err != VK_SUCCESS)
					throw new AssertionError("Failed to create new swapchain: " + Util.translateVulkanError(err));
				
				submitCommandBuffer(queue, setupCommandBuffer);
				vkQueueWaitIdle(queue);
				
				if(framebuffers != null)
					for(int i = 0; i < framebuffers.length; i++)
						vkDestroyFramebuffer(logicalDevice, framebuffers[i], null);
				
				framebuffers = createFramebuffers(logicalDevice, swapchain, renderPass);
				//create render command buffers
				if(renderCommandBuffers != null) 
					vkResetCommandPool(logicalDevice, renderCommandPool, 0);
				
				renderCommandBuffers = createRenderCommandBuffers(logicalDevice, renderCommandPool, swapchain, framebuffers, renderPass, pipeline, verticesBuf);
				
				mustRecreate = false;
			}
		}
		
	public static void initGLFW() {
		if(!glfwInit())
			throw new AssertionError("Failed to init GLFW");
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
	}
		
	/**
	 * <h5>Description:</h5>
	 * <p>Initialize resources, and checks for hardware and software compatibility.</p>
	 */
	private static void init() {
		
		initGLFW();
		
		EngineVersioning.initResources("Triangle Demo", 1, 0, 2);
		HardwareManager.initialize();
		HardwareManager.createDefaultInstance();
		HardwareManager.enumeratePhysicalDevices();
		
		if(HardwareManager.devices.length == 0)
			throw new AssertionError("Failed to detect any device that supports vulkan!");

		physicalDevice = HardwareManager.devices[0];
		physicalDevice.acquireProperties(HardwareManager.getInstance());
		
		graphicsQueueFamilyIndex = physicalDevice.getNextQueueFamilyIndex(0, VK_QUEUE_GRAPHICS_BIT & VK_QUEUE_TRANSFER_BIT);
		if(graphicsQueueFamilyIndex == -1)
			graphicsQueueFamilyIndex = physicalDevice.getNextQueueFamilyIndex(0, VK_QUEUE_GRAPHICS_BIT);
		
		
		String[] extensions = new String[] {VK_KHR_SWAPCHAIN_EXTENSION_NAME};
		ByteBuffer[] bExtensions = Util.makeByteBuffers(extensions);
		PointerBuffer pExtensions = Util.makePointer(bExtensions);
		
		String[] validationLayers = new String[2];
		validationLayers[0] = "VK_LAYER_LUNARG_standard_validation";
		validationLayers[1] = "VK_LAYER_LUNARG_object_tracker";
		
		FloatBuffer queuePriorities = memAllocFloat(1);
		queuePriorities.put(0.0f);
		queuePriorities.flip();
		
		logicalDevice = Util.createLogicalDevice(physicalDevice, graphicsQueueFamilyIndex, queuePriorities, 0, null, pExtensions);		
		
		window = new Window("Primitive demo", 0, 0, width, height);
		
		
		GLFWKeyCallback exitCall = new GLFWKeyCallback() {

			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if(action != GLFW_RELEASE)
					return;
				if(key == GLFW_KEY_ESCAPE)
					glfwSetWindowShouldClose(window, true);
			}
		};
		glfwSetKeyCallback(window.getWindowID(), exitCall);
		
		LongBuffer lb = memAllocLong(1);
		int err = glfwCreateWindowSurface(HardwareManager.getInstance(), window.getWindowID(), null, lb);
		final long surface = lb.get(0);
		if(err < 0)
			throw new AssertionError("Could not obtain surface pointer: " + Util.translateVulkanError(err));
		memFree(lb);
		
		final ColorFormatAndSpace format = Util.getNextColorFormatAndSpace(0, physicalDevice, surface, VK_FORMAT_B8G8R8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);;
		
		final long commandPool = Util.createCommandPool(logicalDevice, logicalDevice.getGraphicsQueueFamilyIndex(), VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		VkCommandBuffer[] buffers = Util.createCommandBuffers(logicalDevice, commandPool, VK_COMMAND_BUFFER_LEVEL_PRIMARY, 2);
		VkCommandBuffer setupCommandBuffer = buffers[0];
		VkCommandBuffer postPresentCommandBuffer = buffers[1];
		VkQueue queue = Util.getDeviceQueue(logicalDevice, logicalDevice.getGraphicsQueueFamilyIndex(), 0);
		long renderPass = createRenderPass(logicalDevice, format.colorFormat);
		long renderCommandPool = Util.createCommandPool(logicalDevice, logicalDevice.getGraphicsQueueFamilyIndex(), VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
		Vertices vertices = createVertices(physicalDevice.memoryProperties, logicalDevice);
		final long pipeline = createPipeline(logicalDevice, renderPass, vertices.createInfo);
        
		window.setVisible(true);
        
		// Pre-allocate everything needed in the render loop
		IntBuffer pImageIndex = memAllocInt(1);
		int currentBuffer = 0;
		PointerBuffer pCommandBuffers = memAllocPointer(1);
		LongBuffer pSwapchains = memAllocLong(1);
		LongBuffer pImageAcquiredSemaphore = memAllocLong(1);
		LongBuffer pRenderCompleteSemaphore = memAllocLong(1);
		
		// Info struct to create a semaphore
        VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
                .pNext(NULL)
                .flags(0);

        // Info struct to submit a command buffer which will wait on the semaphore
        IntBuffer pWaitDstStageMask = memAllocInt(1);
        pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(NULL)
                .waitSemaphoreCount(pImageAcquiredSemaphore.remaining())
                .pWaitSemaphores(pImageAcquiredSemaphore)
                .pWaitDstStageMask(pWaitDstStageMask)
                .pCommandBuffers(pCommandBuffers)
                .pSignalSemaphores(pRenderCompleteSemaphore);

        // Info struct to present the current swapchain image to the display
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc()
                .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pNext(NULL)
                .pWaitSemaphores(pRenderCompleteSemaphore)
                .swapchainCount(pSwapchains.remaining())
                .pSwapchains(pSwapchains)
                .pImageIndices(pImageIndex)
                .pResults(null);
		
        FPSCounter counter = new FPSCounter(1);
        int off = 0;
        
        
        
		while(!glfwWindowShouldClose(window.getWindowID())) {
			
            // Handle window messages. Resize events happen exactly here.
            // So it is safe to use the new swapchain images and framebuffers afterwards.
			glfwPollEvents();
			counter.newFrame();
			if(off > 2000) {
				System.out.println(counter.avrFPS);
				off = 0;
			}
			else
				off++;
			
			if(SwapchainRecreator.mustRecreate || swapchain == null)
				SwapchainRecreator.recreate(setupCommandBuffer, logicalDevice, queue, surface, renderPass, renderCommandPool, format, pipeline, vertices.verticesBuf);
			
			// Create a semaphore to wait for the swapchain to acquire the next image
			err = vkCreateSemaphore(logicalDevice, semaphoreCreateInfo, null, pImageAcquiredSemaphore);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to create semaphore: " + Util.translateVulkanError(err));
			
			// Create a semaphore to wait for the render to complete, before presenting
            err = vkCreateSemaphore(logicalDevice, semaphoreCreateInfo, null, pRenderCompleteSemaphore);
            if (err != VK_SUCCESS) {
                throw new AssertionError("Failed to create render complete semaphore: " + Util.translateVulkanError(err));
            }
			
			// Get next image from the swap chain (back/front buffer).
            // This will setup the imageAquiredSemaphore to be signalled when the operation is complete
			err = vkAcquireNextImageKHR(logicalDevice, swapchain.swapchainHandle, UINT64_MAX, pImageAcquiredSemaphore.get(0), VK_NULL_HANDLE, pImageIndex);
			currentBuffer = pImageIndex.get(0);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to acquire next swapchain image: " + Util.translateVulkanError(err));
			
            // Select the command buffer for the current framebuffer image/attachment
			pCommandBuffers.put(0, renderCommandBuffers[currentBuffer]);
			
			err = vkQueueSubmit(queue, submitInfo, NULL);//NULL_HANDLE
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to submit render queue: " + Util.translateVulkanError(err));
			
			// Present the current buffer to the swap chain
            // This will display the image
			pSwapchains.put(0, swapchain.swapchainHandle);
			err = vkQueuePresentKHR(queue, presentInfo);
			if(err != VK_SUCCESS)
				throw new AssertionError("Failed to present the swapchain image: " + Util.translateVulkanError(err));
			
            // Create and submit post present barrier
			vkQueueWaitIdle(queue);

			vkDestroySemaphore(logicalDevice, pImageAcquiredSemaphore.get(0), null);
			vkDestroySemaphore(logicalDevice, pRenderCompleteSemaphore.get(0), null);
			submitPostPresentBarrier(swapchain.images[currentBuffer], postPresentCommandBuffer, queue);
		}		
		
		
		vkDeviceWaitIdle(logicalDevice);
		
		presentInfo.free();
		memFree(pWaitDstStageMask);
		submitInfo.free();
		memFree(pImageAcquiredSemaphore);
		memFree(pRenderCompleteSemaphore);
		semaphoreCreateInfo.free();
		memFree(pSwapchains);
		memFree(pCommandBuffers);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Destroys contents generated by <code>init()</code> method.</p>
	 */
	private static void destroy() {
		physicalDevice.destroyProperties();
		vkDestroyDevice(logicalDevice, null);
		EngineVersioning.destroy();
		
		HardwareManager.destroyInstance();
		
		glfwDestroyWindow(window.getWindowID());
		glfwTerminate();
	}
	
	public static void main(String[] args) {
		init();
		destroy();
	}
}
