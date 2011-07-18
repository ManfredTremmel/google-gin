/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.inject.client.privatepinned;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.inject.client.PrivateGinModule;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Verify that pinning and exposing work together.
 *
 * <p>Sets up an implicit, unconstrained binding (the Implementation classes)
 * that depends on a pinned, private binding.  The unconstrained binding has to
 * move into the private module; we had errors in the past because it would
 * "escape" the module and drag the pinned binding with it.
 */
public class PrivatePinnedTest extends GWTTestCase {

  public void testBindingsStayInSubModules() throws Exception {
    TestGinjector ginjector = GWT.create(TestGinjector.class);

    Interface1 interface1 = ginjector.getInterface1();
    Interface2 interface2 = ginjector.getInterface2();

    SubImplementation subImplementation1 = interface1.getSubImplementation();
    SubImplementation subImplementation2 = interface2.getSubImplementation();

    assertNotNull(subImplementation1);
    assertNotNull(subImplementation2);

    Interface1 otherInterface1 = ginjector.getInterface1();
    Interface2 otherInterface2 = ginjector.getInterface2();

    assertNotSame(interface1, otherInterface1);
    assertNotSame(interface2, otherInterface2);

    SubImplementation otherSubImplementation1 = otherInterface1.getSubImplementation();
    SubImplementation otherSubImplementation2 = otherInterface2.getSubImplementation();

    assertSame(subImplementation1, otherSubImplementation1);
    assertSame(subImplementation2, otherSubImplementation2);

    assertNotSame(subImplementation1, subImplementation2);
  }

  @GinModules({Module1.class, Module2.class})
  interface TestGinjector extends Ginjector {
    Interface1 getInterface1();
    Interface2 getInterface2();
  }

  static class Module1 extends PrivateGinModule {
    @Override
    protected void configure() {
      bind(Interface1.class).to(Implementation1.class);
      expose(Interface1.class);
      bind(SubImplementation.class).in(Singleton.class);
    }
  }

  static class Module2 extends PrivateGinModule {
    @Override
    protected void configure() {
      bind(Interface2.class).to(Implementation2.class);
      expose(Interface2.class);
      bind(SubImplementation.class).in(Singleton.class);
    }
  }

  interface Interface1 {
    SubImplementation getSubImplementation();
  }

  interface Interface2 {
    SubImplementation getSubImplementation();
  }

  static class Implementation1 implements Interface1 {
    private final SubImplementation subImplementation;

    @Inject
    public Implementation1(SubImplementation subImplementation) {
      this.subImplementation = subImplementation;
    }

    @Override
    public SubImplementation getSubImplementation() {
      return subImplementation;
    }
  }

  static class Implementation2 implements Interface2 {
    private final SubImplementation subImplementation;

    @Inject
    public Implementation2(SubImplementation subImplementation) {
      this.subImplementation = subImplementation;
    }

    @Override
    public SubImplementation getSubImplementation() {
      return subImplementation;
    }
  }

  static class SubImplementation {
  }

  public String getModuleName() {
    return "com.google.gwt.inject.InjectTest";
  }
}
