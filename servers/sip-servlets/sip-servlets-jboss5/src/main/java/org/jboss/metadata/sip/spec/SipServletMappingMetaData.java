/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.metadata.sip.spec;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.metadata.javaee.support.IdMetaDataImpl;

/**
 * sip-app/servlet-mapping metadata
 * 
 * @author jean.deruelle@gmail.com
 * @version $Revision$
 */
public class SipServletMappingMetaData extends IdMetaDataImpl
{
   private static final long serialVersionUID = 1;
   protected String servletName;
   protected List<PatternMetaData> patterns;

   public String getServletName()
   {
      return servletName;
   }
   public void setServletName(String servletName)
   {
      this.servletName = servletName;
   }

   public List<PatternMetaData> getPatterns()
   {
      return patterns;
   }
   @XmlElement(name="pattern")
   public void setPatterns(List<PatternMetaData> patterns)
   {
      this.patterns = patterns;
   }

   public String toString()
   {
      StringBuilder tmp = new StringBuilder("SipServletMappingMetaData(id=");
      tmp.append(getId());
      tmp.append(",servletName=");
      tmp.append(servletName);
      tmp.append(",patterns=");
      tmp.append(patterns);
      tmp.append(')');
      return tmp.toString();
   }

}