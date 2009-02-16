package org.mobicents.slee.container.component.deployment.jaxb.descriptors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.slee.ComponentID;
import javax.slee.management.DeploymentException;
import javax.slee.management.LibraryID;

import org.mobicents.slee.container.component.deployment.jaxb.descriptors.common.MSecurityPermissions;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.common.references.MLibraryRef;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.library.MJar;
import org.mobicents.slee.container.component.deployment.jaxb.descriptors.library.MLibrary;

/**
 * 
 * LibraryDescriptorImpl.java
 *
 * <br>Project:  mobicents
 * <br>3:52:21 AM Jan 30, 2009 
 * <br>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a> 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class LibraryDescriptorImpl {

  private LibraryID libraryID;
  private String description;
  private List<MJar> jars = new ArrayList<MJar>();
  private List<MLibraryRef> libraryRefs;
  private MSecurityPermissions securityPermissions;
  
  private Set<ComponentID> dependenciesSet = new HashSet<ComponentID>();

  public LibraryDescriptorImpl(MLibrary library, MSecurityPermissions securityPermissions, boolean isSlee11) throws DeploymentException
  {
    try
    {
      this.description = library.getDescription();
      this.jars = library.getJar();
      this.libraryRefs = library.getLibraryRef();
      this.securityPermissions = securityPermissions;
      
      this.libraryID = new LibraryID(library.getLibraryName(), library.getLibraryVendor(),library.getLibraryVersion());
      
      buildDependenciesSet();
    }
    catch (Exception e) {
      throw new DeploymentException("Failed to build library descriptor", e);      
    }
  }
  
  private void buildDependenciesSet()
  {
    for(MLibraryRef libraryRef : libraryRefs)
    {
      this.dependenciesSet.add( libraryRef.getComponentID() );
    }
  }

  public String getDescription()
  {
    return description;
  }
  
  public List<MJar> getJars()
  {
    return jars;
  }
  
  public LibraryID getLibraryID() {
	return libraryID;
  }
  
  public MSecurityPermissions getSecurityPermissions()
  {
    return this.securityPermissions;
  }
  
  public Set<ComponentID> getDependenciesSet()
  {
    return this.dependenciesSet;
  }
  
}
