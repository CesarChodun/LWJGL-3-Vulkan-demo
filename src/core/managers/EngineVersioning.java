package core.managers;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.vulkan.VkApplicationInfo;

/**
 *	<h5>Description:</h5>
 *	<p>This class contains informations about <b>JVEngine</b>.</p>
 * @author Cezary Choduń
 */
public class EngineVersioning {

	public static final int API_MAJOR = 1, API_MINOR = 0, API_PATCH = 2;
	public static final int ENGINE_MAJOR = 0, ENGINE_MINOR = 1, ENGINE_PATCH = 3;
	public static final String ENGINE_NAME = "JVEngine";
	
	public static String AppName = "Application";
	public static int appMajor = 0, appMinor = 1, appPatch = 0;
	private static VkApplicationInfo appInfo = null;
	
	//TODO Store validation layers and VkInstance
	
	/**
	 * <h5>Description:</h5>
	 * <p>Initialize <code><b><i>VkApplicationInfo</i></b><code>.</p>
	 * <p><b>Note: </b> This method <b>should</b> be invoked only once.</p>
	 * @param applicationName 	- Name of the newly created application(by default: "Application").
	 * @param appMajor			- Major version of the application(by default: <b>0</b>). 
	 * @param appMinor			- Minor version of the application(by default: <b>1</b>). 
	 * @param appPatch			- Patch version of the application(by default: <b>0</b>). 
	 * 
	 * @see {@link org.lwjgl.vulkan.VkApplicationInfo}
	 */
	public static void initResources(String applicationName, int appMajor, int appMinor, int appPatch) {
		appInfo = VkApplicationInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
				.apiVersion(VK_MAKE_VERSION(API_MAJOR, API_MINOR, API_PATCH))
				.pEngineName(memUTF8(ENGINE_NAME))
				.engineVersion(VK_MAKE_VERSION(ENGINE_MAJOR, ENGINE_MINOR, ENGINE_PATCH))
				.pApplicationName(memUTF8(applicationName))
				.applicationVersion(VK_MAKE_VERSION(appMajor, appMinor, appPatch));
		
		EngineVersioning.AppName = applicationName;
		EngineVersioning.appMajor = appMajor;
		EngineVersioning.appMinor = appMinor;
		EngineVersioning.appPatch = appPatch;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Frees previously allocated resources.</p>
	 * <p><b>Note: </b> After this call class <b>should not</b> be used again.</p>
	 */
	public static void destroy() {
		if(appInfo == null)
			return;
		
		memFree(appInfo.pApplicationName());
		memFree(appInfo.pEngineName());
		appInfo = null;
	}

	public static VkApplicationInfo getAppInfo() {
		return appInfo;
	}
}
