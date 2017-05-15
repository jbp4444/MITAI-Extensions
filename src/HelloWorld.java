// Copyright 2017, John Pormann, Duke University
//
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
//
// borrowed from several java files in main-line AppInventor source
// that code is Copyright 2011-2014 MIT, All rights reserved

package edu.duke.appinventor;

import com.google.appinventor.components.runtime.*;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

// the AppInventor class ...

@DesignerComponent(version = HelloWorld.VERSION,
  description = "This extension provides a Hello-World function",
  category = ComponentCategory.EXTENSION,
  nonVisible = true,
  iconName = "images/extension.png")

@SimpleObject( external=true )
public final class HelloWorld extends AndroidNonvisibleComponent {

  public static final int VERSION = 1;

  public HelloWorld(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Returns Hello World + input" )
  public String SayHello( String input ) {
    return( new String("Hello World "+input) );
  }

}
