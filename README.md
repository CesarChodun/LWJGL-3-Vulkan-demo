# JVEngine <img align="right" src = "other_resources/supported_systems.png" width="192" height="64"/> 

<h2>Goal</h2>
<p>The aim of the project is to create simple <b>game engine</b> that is entirely based on <b>Vulkan</b> api.</p>
<h2>Requirements</h2>
<p><b>Engine</b> is written in <b>Java</b> and is using <b>LWJGL 3</b>. So before you start you should install <b>Java</b> and set up required <b>LWJGL 3</b> libraries(see <b>Required LWJGL libraries</b> bellow).</p>

<h2>Version 0.1.2</h2>
<p>JVEngine is currently in an alfa stage and therefor many functionalities are not added yet. But I highly encourage people new to <b>Vulkan</b> to take a closer look at(at least) some functionalities of JVEngine. For example choosing the right physical device is often very confusing for many beginners, and JVEngine has this function already implemented!</p>

<h2>Required LWJGL libraries</h2>
<p> It is highly recomended to download default "Minimal Vulkan" preset with JOML selected. You can find it on this website:
https://www.lwjgl.org/. </p>

<h2>Instalation</h2>

 <h3>Prepare your IDE</h3>
 
  * Download and install Vulkan from <a href="www.google.com">this</a> site.
  * Download required LWJGL libraries("Minimal Vulkan" + JOML) from <a href="TODO">this</a> page.
  * Create a new project in your IDE.
  * Add the Vulkan libraries to the project.
  
  <h3>Run demo</h3>
  
  * Clone the repository.
  * Transfer files from source("src") to your's project source folder.
  * Refresh/reopen your IDE to see changes.
  * Navigate to the desired demo class.
  * Run the demo.
  * If you are using Mac OS you might have to add "-XstartOnTheFirstThread" to the VM arguments and then run the demo again.
  
 
 <h2>Troubleshooting</h2>
 
 There are multiple things that might go wrong during the installation process.
 First of all you have to determine whether the problem is due to wrong installation or the demo is malfunctioning.
 
  <h3>Installation check #1 </h3>
 
   Run the <a href="https://github.com/CesarChodun/JVEngine/blob/master/src/demos/InitializationDemo.java">InitializationDemo.java</a>. If you can see a window proceed to installation check #2.
   Otherwise check whether there are any errors preventing you from compiling the code.
   If so try to remove the libraries from the project and then add them again and recompile the project.
   Try adding the "-XstartOnFirstThread" argument(or removing it).
   If the problem is still present go to the section "Issues reporting".
   
  <h3>Installation check #2 </h3>
  
  TODO(Readme will be updated soon)

<h2>Contribute!</h2>
<p>Developing fully functional game engine is challenging enough for a team of full time developers. Not to say for a guy that is pursuing bechelor's degree at the same time(and hase some other projects too). So any amount of help is more then welcome!</p>

<h2>Gallery</h2>

![triangle_git](other_resources/suzanne.jpg)

![triangle_git](other_resources/triangle_git.jpg)

![triangle_git](other_resources/quad_git.jpg)
