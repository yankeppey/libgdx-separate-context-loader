# libgdx-separate-context-loader
Asset loader that allow you to load textures (framebuffers, etc) in a separate gl context in your Libgdx application.

What I tried to do here is to load textures in a separate GL context, not interrupting main rendering thread, and then 
use them in the original (shared) GL context. Thus, no "loading" screen needed, and you can load textures seamlessly.

However, it seems that different devices work a bit differently with separate gl contexts:

- Nexus 7 (2013) works ok only if we init egl context once (initContext() in SeparateContextAssetLoader), without destroying 
it in the end of texture loading  (endContext() in SeparateContextAssetLoader). Otherwise eglMakeCurrent or eglCreateContext 
can freeze the thread and not return anything.

- Samsung T-231 (or T-230 or possibly similiar devices) behaves in the opposite way. 
if we init egl context before loading every texture, and destroy it after, all works OK.  Otherwise, if we just 
init egl context and use it for every textures, it has some artefacts like missing textures etc. 

I didn't test it on other devices. If you want to resolve this issue, you possibly want 
- either build the fulllist of models and to check how it behaves on every model
- or to correct this code to let it run everywhere

This code possibly doesn't work out of the box, cause I just cut it out from one proprietary projectwith a very little 
preparations, but I think it's easy to copy needed files into your project.

