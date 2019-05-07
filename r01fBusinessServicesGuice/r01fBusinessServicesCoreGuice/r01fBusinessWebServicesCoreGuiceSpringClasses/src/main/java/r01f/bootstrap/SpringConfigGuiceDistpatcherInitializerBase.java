package r01f.bootstrap;

import r01f.servlet.spring.SpringConfigDistpacherInitializerBase;
import r01f.servlet.spring.SpringWebMvcComponent;

public abstract class SpringConfigGuiceDistpatcherInitializerBase<R extends SpringRootConfigBootstrapGuiceBase, MVC extends SpringWebMvcComponent>
      extends SpringConfigDistpacherInitializerBase<R,MVC> {
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public SpringConfigGuiceDistpatcherInitializerBase( final Class<R> classType, final  Class<MVC> mvvcComponent) {
		super(classType,mvvcComponent);
	}
}
