
# Ikasan Standalone Developer Guide

# Introduction

## Overview

The Ikasan Enterprise Integration Platform (IkasanEIP) provides a robust and scalable ESB platform based on open technologies and standards. 
IkasanEIP can be adopted as little or as much as required to meet your integration needs.

## About

This guide demonstrates the IkasanESB features by through a hands-on Ikasan Application build from scratch to a fully working solution.

_NOTE: This is an example to demonstrate the development and core features of Ikasan - it is not intended to produce a production ready solution._

This is part of the documentation suite for the Ikasan Enterprise Integration Platform.

## Audience

This guide is targeted at developers wishing to get started and undertake their first development projects with the Ikasan Enterprise Integration Platform.

A familiarity with Java and Maven is assumed.

## How to Use This Guide

This guide provides a quick and concise series of steps for getting started with the IkasanEIP platform. These steps should be followed sequentially. Points to note and other hints/tips are provided as additional information or best practices, however, full details on these aspects are beyond the scope of this document.

On completion of this document the reader should have installed and configured all required software for development and have created and run an Ikasan Application.

# Development Environment

You can choose to install and configure the tooling on your machine as is your preference – the following is recommended simply as a guide providing a split between the development tools; your development sandbox area; and your development runtime environment.
```
${root_install_dir}/${devtools_install_dir}
                   /${runtime_dir}
                   /${sandbox_dir}
```

where,

${devtools_install_dir} - contains your development tools installs i.e. Java, Maven

${runtime_dir}  - is your runtime environment for deployment

${sandbox_dir}  - is your area for creating and building Integration Modules

For convenience we will refer to the base directories above throughout this guide.

# Development Tools

This section assumes you have nothing currently in place. If you do have any of the tools installed/configured then please check the setup and versions to ensure they are compatible.

## Java Development Kit/Runtime Environment

IkasanEIP is built in Java (JDK) and runs on the Java Runtime Environment (JRE).

IkasanEIP will support any language which compiles to bytecode and runs within the Java Virtual Machine (JVM).

For more details on Java see [http://www.oracle.com./technetwork/java](http://www.oracle.com./technetwork/java)

### Version

IkasanEIP version 2.x.y+ requires Java 1.8.x.

### Installation

Download the JDK appropriate for your Operating System.

All Java JDK downloads are available from [http://www.oracle.com./technetwork/java/javase/downloads/index.html](http://www.oracle.com./technetwork/java/javase/downloads/index.html)

Install by following the JDK installation instructions.

We will refer to the java install directory as ${devtools\_install\_dir}/java although depending on your OS and installer the location may differ.  For instance, on Windows this may be under "_Program Files\Java_".

The important point is that we have the JAVA_HOME and PATH environment variables set to reflect this java install.

#### Setting Unix Environment Variables
```
export JAVA_HOME=${root_install_dir}/${platform_install_dir}/<java install dir name>;
export PATH=$JAVA_HOME/bin;$PATH |
```

#### Setting Windows Environment Variables
```
set JAVA_HOME=%root_install_dir%\%platform_install_dir%\<java install dir name>
set PATH=%JAVA_HOME%\bin:%PATH% 
```

### Sanity Checks

You can test your installation by starting a new command line session and typing,
```
  java –version 
```

This should reflect the Java JDK version you have just installed.

For instance – the line in bold depicts the important information.

```
java version "1.8.0_31"
Java(TM) SE Runtime Environment (build 1.8.0_31-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.31-b07, mixed mode)
```

## Maven

IkasanEIP uses Maven for its build and build time dependency management.

For more details on Maven see [http://maven.apache.org](http://maven.apache.org/)

### Version

IkasanEIP version 2.x.y+ requires Maven 3.3.x.

### Installation

Download the latest 3.3.x Maven binary from [http://maven.apache.org/download.cgi](http://maven.apache.org/download.cgi)

Unzip the image under ${devtools\_install\_dir}.

The M2_HOME and PATH environment variables should be set to reflect this maven install.

#### Setting Environment Variables From UNIX Command Line

```
export M2_HOME=${root_install_dir}/${platform_install_dir}/<maven install dir name>
export PATH=$M2_HOME/bin;$PATH 
```

#### Setting Environment Variables From Windows Command Line

```
set M2_HOME=${root_install_dir}/${platform_install_dir}\<maven install dir name>
set PATH=%M2_HOME%\bin;%PATH% 
```

### Sanity Checks

You can test your installation by starting a new command line session and typing,

```
mvn --version 
```

This should reflect the Maven version you have just installed.

For instance – the bold output depicts the important information,

```
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T16:41:47+00:00)
Maven home: /usr/local/Cellar/maven/3.3.9/libexec
Java version: 1.8.0_31, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_31.jdk/Contents/Home/jre
Default locale: en_GB, platform encoding: UTF-8
OS name: "mac os x", version: "10.12.6", arch: "x86_64", family: "mac"
```

### update mvn settings.xml 

If you using Snapshot versions, please make sure you refer to oss repo in your .m2/settings.xml
```xml  
<profile>
  <id>securecentral</id>
  <repositories>
      <repository>
          <id>ikasaneip-snapshots</id>
          <url>http://oss.sonatype.org/content/repositories/snapshots/</url>
          <releases>
              <enabled>false</enabled>
          </releases>
          <snapshots>
              <enabled>true</enabled>
          </snapshots>
      </repository>
      <repository>
          <id>ikasaneip-releases</id>
          <url>http://oss.sonatype.org/content/repositories/releases/</url>
          <releases>
              <enabled>true</enabled>
          </releases>
          <snapshots>
              <enabled>false</enabled>
          </snapshots>
      </repository>
      <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases>
              <enabled>true</enabled>
          </releases>
      </repository>
  </repositories>
  <pluginRepositories>
      <pluginRepository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
          <releases>
              <enabled>true</enabled>
          </releases>
      </pluginRepository>
  </pluginRepositories>
</profile>
        
 <activeProfiles>
    <activeProfile>securecentral</activeProfile>
 </activeProfiles>
```
 

## Java IDE
Any Java IDE will suffice (even a text editor if you are feeling brave), we recommend IntelliJ CE.
 

## Problem Definition
To begin we can define a simple problem to give some context to the demo. 
Typically the problem domain addressed by IkasanESB requires the sourcing of business events from one application followed by various conversions and orchestrations to other downstream applications.



## Design
We will create an Ikasan Integration Module which consists of a single flow containing two component operations.

    IntegrationModule
    - Flow
        - Consumer -> Producer
        
For simplicity we will utilise some Ikasan off-the-shelf consumers to mock application event generation as a source; and off-the-shelf producers to mock event publication as a target.

## Implementation
We will be using IntelliJ for the rest of this demonstration.

### Create a Project
In IntelliJ select `File/New/Project...`

Select maven (blank archetype). Ensure you select JDK 1.8 as the SDK for this new project.
![Login](quickstart-images/IntelliJ-new-project-screen1.png) 

Specify your project Maven coordinates such as Groupid, ArtefactId, and Version.

For instance, 
- GroupId --> `com.ikasan.example`
- ArtefactId --> `MyIntegrationModule`
- Version --> `1.0.0-SNAPSHOT`
![Login](quickstart-images/IntelliJ-new-project-screen2.png) 

Select the directory/workspace to create this project.
![Login](quickstart-images/IntelliJ-new-project-screen3.png) 

Thats it! 

Your project will be created and look something like this.
![Login](quickstart-images/IntelliJ-new-project-screen4.png) 

We now have a blank Java project based on a Maven project structure.
Next we need to update the Maven pom.xml to set the application packaging to create a Jar.
```
<?xml version="1.0" encoding="UTF-8"?>
      <project xmlns="http://maven.apache.org/POM/4.0.0"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
          <modelVersion>4.0.0</modelVersion>
      
          <groupId>com.ikasan.example</groupId>
          <artifactId>MyIntegrationModule</artifactId>
          <version>1.0.0-SNAPSHOT</version>
          <packaging>jar</packaging>        <!-- Add packaging type to create a jar -->
      
      </project>
```

Now add the first Ikasan Application dependency to the Maven pom.xml.
 
Edit the pom.xml and add the ikasan-eip-standalone dependency as well as the build plugin execution requirement.
```
<?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>
   
       <groupId>com.ikasan.example</groupId>
       <artifactId>MyIntegrationModule</artifactId>
       <version>1.0.0-SNAPSHOT</version>
       <packaging>jar</packaging>
   
       <!-- Add project dependencies -->
       <dependencies>
       
           <!-- Add IkasanESB core library dependency -->
           <dependency>
               <groupId>org.ikasan</groupId>
               <artifactId>ikasan-eip-standalone</artifactId>
               <version>2.0.0</version>
           </dependency>
           
           <!-- Use Ikasan h2 persistence (Do not use in production) -->
           <dependency>
               <groupId>org.ikasan</groupId>
               <artifactId>ikasan-h2-standalone-persistence</artifactId>
               <version>2.0.0</version>
           </dependency>
                   
       </dependencies>
   
       <!-- Add project build plugin -->
       <build>
           <plugins>
               <plugin>
                   <groupId>org.springframework.boot</groupId>
                   <artifactId>spring-boot-maven-plugin</artifactId>
                   <version>1.5.6.RELEASE</version>
                   <executions>
                       <execution>
                           <goals>
                               <goal>repackage</goal>
                           </goals>
                       </execution>
                   </executions>
               </plugin>
           </plugins>
       </build>
       
   </project>
```

Now create a class with a fully qualified name of ```com.ikasan.example.MyApplication``` to instantiate the Ikasan Application.

Copy and paste the entirety of the code below replacing the content of that class.
```
package com.ikasan.example;

import org.ikasan.builder.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@ComponentScan({"org.ikasan", "com.ikasan"})
@ImportResource({ "classpath:h2-datasource-conf.xml" })
public class MyApplication
{
    public static void main(String[] args)
    {
        // Create instance of an ikasan application
        IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);
    }
}  
```

Provide some configuration properties for the module by creating a resources/application.properties
```
# Logging levels across packages (optional)
logging.level.com.arjuna=INFO
logging.level.org.springframework=INFO

# Blue console servlet settings (optional)
server.error.whitelabel.enabled=false

# Web Bindings
server.port=8090
server.address=localhost
server.contextPath=/example-im

# health probs and remote management (optional)
management.security.enabled=false
management.context-path=/manage
endpoints.shutdown.enabled=true

# Ikasan persistence store
datasource.username=sa
datasource.password=sa
datasource.driver-class-name=org.h2.Driver
datasource.xadriver-class-name=org.h2.jdbcx.JdbcDataSource
datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
datasource.dialect=org.hibernate.dialect.H2Dialect
datasource.show-sql=true
datasource.hbm2ddl.auto=create
datasource.validationQuery=select 1
```

The web binding section in ```application.properties``` is particularly important and you should ensure you choose a free server.port to bind to.

Build and run the application,
From IntelliJ just right click on MyApplication and select run ```MyApplication.main()```

Alternatively to run from the command line ensure you are in the project root directory i.e. MyIntegrationModule then run a Maven clean install
```
mvn clean install
```

If successful run the application
```
java -jar target/MyIntegrationModule-1.0.0-SNAPSHOT.jar
```

Try accessing the application through a browser at ```http://localhost:8090/example-im``` (user:admin password:admin)
![Login](quickstart-images/new-project-embeddedConsole-screen5.png) 

Logging in will give you the default home page.
![Login](quickstart-images/new-project-embeddedConsole-screen6.png) 

Selecting 'Modules' will show the Integration Module for this example, but as we haven't defined one you just see a fairly empty screen.
![Login](quickstart-images/new-project-embeddedConsole-screen7.png) 
 
Lets define a module.

Go back to the MyApplication class in IntelliJ and update it to the following
```
package com.ikasan.example;

import org.ikasan.builder.*;
import org.ikasan.builder.component.ComponentBuilder;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@ComponentScan({"org.ikasan", "com.ikasan"})
@ImportResource({ "classpath:h2-datasource-conf.xml", "classpath:ikasan-transaction-pointcut-eventListener.xml" })
public class MyApplication
{
    public static void main(String[] args)
    {
        // Create instance of an ikasan application
        IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

        // Get an instance of a builder factory from the application which will
        // provide access to all required builders for the application
        BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();

        // Create a module builder from the builder factory
        ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder("My Integration Module")
                .withDescription("My first integration module.");

        // Create a component builder from the builder factory
        ComponentBuilder componentBuilder = builderFactory.getComponentBuilder();

        // create a flow from the module builder and add required orchestration components
        Flow eventGeneratingFlow = moduleBuilder.getFlowBuilder("EventGeneratingFlow")
                .consumer("My Source Consumer", componentBuilder.eventGeneratingConsumer().build())
                .producer("My Target Producer", componentBuilder.logProducer().build())
                .build();

        // Add the created flow to the module builder and create the module
        Module module = moduleBuilder
                .addFlow(eventGeneratingFlow)
                .build();

        // Pass the module to the Ikasan Application for execution
        ikasanApplication.run(module);
    }
}
```

Build and run the application again.

From IntelliJ just right click on MyApplication and select run ```MyApplication.main()```

Alternatively to run from the command line ensure you are in the project root directory i.e. MyIntegrationModule then run a Maven clean install
```
mvn clean install
```

Refresh your browser at ```http://localhost:8090/example-im```, login and select 'Modules' - you now see your new module.
![Login](quickstart-images/new-project-embeddedConsole-screen9.png)

Selecting the module will show you the flows within this module which can be started, stopped, paused/resumed.
![Login](quickstart-images/new-project-embeddedConsole-screen10.png)

Start the flow - you will see the state change from 'stopped' to 'running'. 
![Login](quickstart-images/new-project-embeddedConsole-screen11.png)

View the application logs (either in IntelliJ or the command line, depending on how the application was started).
![Login](quickstart-images/new-project-embeddedConsole-screen12.png)

Congratulations - your first working Ikasan Integration Module, but what is it doing...

Lets go back to the code, specifically the ```main()``` method to understand what we just implemented and ran.

```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);
       ...
   }
```
Firstly, we get an instance of an IkasanAppication from the IkasanApplicationFactory. This ikasanApplication will 
provision everything we need to instantiate and run this application.


```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

       // Get an instance of a builder factory from the application which will
       // provide access to all required builders for the application
       BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();
       ...
   }
```
We then get an instance of a builderFactory from the application. 

This builderFactory will provide all other builders required to create modules, flows, and components.

The first thing to create is a moduleBuilder from the builderFactory. When we create the moduleBuilder we provide the name we are going to assign to the module.
We can also set other properties on the module through this moduleBuilder such as description.

```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

       // Get an instance of a builder factory from the application which will
       // provide access to all required builders for the application
       BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();

       // Create a module builder from the builder factory
       ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder("My Integration Module")
               .withDescription("My first integration module.");
       ...
   }
```
Next, get a componentBuilder instance from the builderFactory - we will be using this in the flowBuilder.
```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

       // Get an instance of a builder factory from the application which will
       // provide access to all required builders for the application
       BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();

       // Create a module builder from the builder factory
       ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder("My Integration Module")
               .withDescription("My first integration module.");

       // Create a component builder from the builder factory
       ComponentBuilder componentBuilder = builderFactory.getComponentBuilder();
       ...
   }
```

Now on to the interesting parts. 
We use the moduleBuilder to get a flowBuilder and provide the name of the flow.
The components within the flow are then added as consumer and producer, both from the componentBuilder.
Each component is given a name and a functional class that does the work. The component classes below are off-the-shelf 
Ikasan components, however, your own components can be easily written and added as shown later.
```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

       // Get an instance of a builder factory from the application which will
       // provide access to all required builders for the application
       BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();

       // Create a module builder from the builder factory
       ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder("My Integration Module")
               .withDescription("My first integration module.");

       // Create a component builder from the builder factory
       ComponentBuilder componentBuilder = builderFactory.getComponentBuilder();

       // create a flow from the module builder and add required orchestration components
       Flow eventGeneratingFlow = moduleBuilder.getFlowBuilder("EventGeneratingFlow")
               .consumer("My Source Consumer", componentBuilder.eventGeneratingConsumer().build())
               .producer("My Target Producer", componentBuilder.logProducer().build())
               .build();
       ...
   }
```
Each off-the-shelf component has ```build()``` called against it as has the flowBuilder to create the flow.
 
```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

       // Get an instance of a builder factory from the application which will
       // provide access to all required builders for the application
       BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();

       // Create a module builder from the builder factory
       ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder("My Integration Module")
               .withDescription("My first integration module.");

       // Create a component builder from the builder factory
       ComponentBuilder componentBuilder = builderFactory.getComponentBuilder();

       // create a flow from the module builder and add required orchestration components
       Flow eventGeneratingFlow = moduleBuilder.getFlowBuilder("EventGeneratingFlow")
               .consumer("My Source Consumer", componentBuilder.eventGeneratingConsumer().build())
               .producer("My Target Producer", componentBuilder.logProducer().build())
               .build();

       // Add the created flow to the module builder and create the module
       Module module = moduleBuilder
               .addFlow(eventGeneratingFlow)
               .build();
       ...
   }
```
 Now we have the flow we can add it to the moduleBuilder and ```build()``` the module.
 
```
   public static void main(String[] args)
   {
       // Create instance of an ikasan application
       IkasanApplication ikasanApplication = IkasanApplicationFactory.getIkasanApplication(MyApplication.class);

       // Get an instance of a builder factory from the application which will
       // provide access to all required builders for the application
       BuilderFactory builderFactory = ikasanApplication.getBuilderFactory();

       // Create a module builder from the builder factory
       ModuleBuilder moduleBuilder = builderFactory.getModuleBuilder("My Integration Module")
               .withDescription("My first integration module.");

       // Create a component builder from the builder factory
       ComponentBuilder componentBuilder = builderFactory.getComponentBuilder();

       // create a flow from the module builder and add required orchestration components
       Flow eventGeneratingFlow = moduleBuilder.getFlowBuilder("EventGeneratingFlow")
               .consumer("My Source Consumer", componentBuilder.eventGeneratingConsumer().build())
               .producer("My Target Producer", componentBuilder.logProducer().build())
               .build();

       // Add the created flow to the module builder and create the module
       Module module = moduleBuilder
               .addFlow(eventGeneratingFlow)
               .build();

       // Pass the module to the Ikasan Application for execution
       ikasanApplication.run(module);
   }
```
Finally we pass the module to the ```ikasanApplication.run()``` method to execute.

That is how every Ikasan Application Integration Module is created regardless of how simple or complex the operations.

## Adding your own Components
Lets add a custom Converter between the Consumer and Producer components.

To do this we simply need to update the flowBuilder lines to insert this new component.
```
        // create a flow from the module builder and add required orchestration components
        Flow eventGeneratingFlow = moduleBuilder.getFlowBuilder("EventGeneratingFlow")
                .consumer("My Source Consumer", componentBuilder.eventGeneratingConsumer().build())
                .converter("My Converter", new MyConverter())
                .producer("My Target Producer", componentBuilder.logProducer().build())
                .build();
```
And implement the ```com.ikasan.example.converter.MyConverter``` class.
To implement the class as an Ikasan Converter component we simply need to use the Converter contract through ```implement Converter```
The contract for the Converter allows for the provision of generic types defining the incoming type and the return type.
In the example below the Converter is expecting an incoming String which it will convert and return as an Integer.

```
package com.ikasan.example.converter;

import org.ikasan.spec.component.transformation.Converter;
import org.ikasan.spec.component.transformation.TransformationException;

public class MyConverter implements Converter<String,Integer>
{
    public Integer convert(String payload) throws TransformationException 
    {
        String[] strings = payload.split(" ");
        int intPart = Integer.valueOf( strings[1] );
        return Integer.valueOf(intPart);
    }
}
```

Build it, run it, and start the flow from the Browser and see what gets output to the logs by the LogProducer.

You will notice that the payload= has changed from the 'Message 1' etc, to simply just the integer part of the message from the consumer.

## Exception Handling
Every Ikasan flow has an automatically create 'Recovery Manager'. The role of the Recovery Manager is to decide what to do if things fail at runtime i.e. an Exception occurs in the flow or any of its components.

Lets see what happens if we get our Converter to throw an exception on a specific message i.e message 5.
```
    public Integer convert(String payload) throws TransformationException
    {
        String[] strings = payload.split(" ");
        int intPart = Integer.valueOf( strings[1] );
        if(intPart == 5)
        {
            throw new TransformationException("error - bad number received [" + 5 + "]");
        }
        return Integer.valueOf(intPart);
    }
```
Build it, run it, and start the flow from the Browser.

If you look at the logs you will see that when it gets to message 5 an exception occurs 
and the Recovery Manager kicked in to decide what to do. 
```
RecoveryManager resolving to [Stop] for componentName[My Converter] exception [error - bad number received [5]]

org.ikasan.spec.component.transformation.TransformationException: error - bad number received [5]
	at com.ikasan.example.converter.MyConverter.convert(MyConverter.java:14) ~[classes/:na]
	at com.ikasan.example.converter.MyConverter.convert(MyConverter.java:6) ~[classes/:na]
	at org.ikasan.flow.visitorPattern.invoker.ConverterFlowElementInvoker.invoke(ConverterFlowElementInvoker.java:121) ~[ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.flow.visitorPattern.VisitingInvokerFlow.invoke(VisitingInvokerFlow.java:860) [ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.flow.visitorPattern.VisitingInvokerFlow.invoke(VisitingInvokerFlow.java:778) [ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.flow.visitorPattern.VisitingInvokerFlow.invoke(VisitingInvokerFlow.java:76) [ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.component.endpoint.util.consumer.EventGeneratingConsumer.onMessage(EventGeneratingConsumer.java:190) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.component.endpoint.util.consumer.EventGeneratingConsumer.onMessage(EventGeneratingConsumer.java:61) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_111]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_111]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_111]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_111]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:333) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:99) [spring-tx-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:282) [spring-tx-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:96) [spring-tx-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:213) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at com.sun.proxy.$Proxy87.onMessage(Unknown Source) [na:na]
	at org.ikasan.component.endpoint.util.consumer.SimpleMessageGenerator.execute(SimpleMessageGenerator.java:95) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.component.endpoint.util.consumer.SimpleMessageGenerator.run(SimpleMessageGenerator.java:83) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_111]
	at java.util.concurrent.FutureTask.run(FutureTask.java:266) [na:1.8.0_111]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_111]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_111]
	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_111]
```
In this case because we havent told the Recovery Manager to take specific action on this type of exception
it resolves to stopping the flow in error. This stoppedInError state can also be seen on the Console.
![Login](quickstart-images/new-project-embeddedConsole-screen13.png)

Lets tell the Recovery Manager to take a different action on this exception.
As this is a configuration on the flow we need to update the code on the flowBuilder to add
a different exceptionResolver. In this case when we see a TransformationException we are now going to exclude the event
that caused the exception.
```
        // create a flow from the module builder and add required orchestration components
        Flow eventGeneratingFlow = moduleBuilder.getFlowBuilder("EventGeneratingFlow")
                .withExceptionResolver(builderFactory
                        .getExceptionResolverBuilder()
                        .addExceptionToAction(TransformationException.class, OnException.excludeEvent()).build())
                .consumer("My Source Consumer", componentBuilder.eventGeneratingConsumer().build())
                .converter("My Converter", new MyConverter())
                .producer("My Target Producer", componentBuilder.logProducer().build())
                .build();
```
Build it, run it, and start the flow from the Browser.

Now we see the same error occuring, but the Recovery Manager excludes the event and allows the flow to keep running.
If you look in the logs you will see something like this,
```
2018-04-14 11:57:51.681  INFO 4811 --- [0.1-8090-exec-2] o.i.f.v.VisitingInvokerFlow              : Started Flow[EventGeneratingFlow] in Module[My Integration Module]
2018-04-14 11:57:51.687  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 1, relatedIdentifier=null, timestamp=1523703471683, payload=1]
2018-04-14 11:57:52.692  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 2, relatedIdentifier=null, timestamp=1523703472692, payload=2]
2018-04-14 11:57:53.696  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 3, relatedIdentifier=null, timestamp=1523703473696, payload=3]
2018-04-14 11:57:54.701  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 4, relatedIdentifier=null, timestamp=1523703474701, payload=4]
2018-04-14 11:57:55.711  INFO 4811 --- [pool-4-thread-1] o.i.recovery.ScheduledRecoveryManager    : RecoveryManager resolving to [ExcludeEvent] for componentName[My Converter] exception [error - bad number received [5]]

org.ikasan.spec.component.transformation.TransformationException: error - bad number received [5]
	at com.ikasan.example.converter.MyConverter.convert(MyConverter.java:14) ~[classes/:na]
	at com.ikasan.example.converter.MyConverter.convert(MyConverter.java:6) ~[classes/:na]
	at org.ikasan.flow.visitorPattern.invoker.ConverterFlowElementInvoker.invoke(ConverterFlowElementInvoker.java:121) ~[ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.flow.visitorPattern.VisitingInvokerFlow.invoke(VisitingInvokerFlow.java:860) [ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.flow.visitorPattern.VisitingInvokerFlow.invoke(VisitingInvokerFlow.java:778) [ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.flow.visitorPattern.VisitingInvokerFlow.invoke(VisitingInvokerFlow.java:76) [ikasan-flow-visitorPattern-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.component.endpoint.util.consumer.EventGeneratingConsumer.onMessage(EventGeneratingConsumer.java:190) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.component.endpoint.util.consumer.EventGeneratingConsumer.onMessage(EventGeneratingConsumer.java:61) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_111]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_111]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_111]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_111]
	at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:333) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.invokeJoinpoint(ReflectiveMethodInvocation.java:190) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:157) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.transaction.interceptor.TransactionInterceptor$1.proceedWithInvocation(TransactionInterceptor.java:99) [spring-tx-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:282) [spring-tx-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:96) [spring-tx-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:213) [spring-aop-4.3.10.RELEASE.jar:4.3.10.RELEASE]
	at com.sun.proxy.$Proxy87.onMessage(Unknown Source) [na:na]
	at org.ikasan.component.endpoint.util.consumer.SimpleMessageGenerator.execute(SimpleMessageGenerator.java:95) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at org.ikasan.component.endpoint.util.consumer.SimpleMessageGenerator.run(SimpleMessageGenerator.java:83) [ikasan-utility-endpoint-2.1.0-SNAPSHOT.jar:na]
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511) [na:1.8.0_111]
	at java.util.concurrent.FutureTask.run(FutureTask.java:266) [na:1.8.0_111]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_111]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_111]
	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_111]

Hibernate: select erroroccur_.Uri, erroroccur_.ModuleName as ModuleNa2_2_, erroroccur_.FlowName as FlowName3_2_, erroroccur_.FlowElementName as FlowElem4_2_, erroroccur_.ErrorDetail as ErrorDet5_2_, erroroccur_.ErrorMessage as ErrorMes6_2_, erroroccur_.ExceptionClass as Exceptio7_2_, erroroccur_.EventLifeIdentifier as EventLif8_2_, erroroccur_.EventRelatedIdentifier as EventRel9_2_, erroroccur_.Action as Action10_2_, erroroccur_.Event as Event11_2_, erroroccur_.EventAsString as EventAs12_2_, erroroccur_.Timestamp as Timesta13_2_, erroroccur_.Expiry as Expiry14_2_, erroroccur_.UserAction as UserAct15_2_, erroroccur_.ActionedBy as Actione16_2_, erroroccur_.UserActionTimestamp as UserAct17_2_, erroroccur_.Harvested as Harvest18_2_ from ErrorOccurrence erroroccur_ where erroroccur_.Uri=?
Hibernate: insert into ErrorOccurrence (ModuleName, FlowName, FlowElementName, ErrorDetail, ErrorMessage, ExceptionClass, EventLifeIdentifier, EventRelatedIdentifier, Action, Event, EventAsString, Timestamp, Expiry, UserAction, ActionedBy, UserActionTimestamp, Harvested, Uri) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: insert into ExclusionEvent (Id, ModuleName, FlowName, Identifier, Event, Timestamp, ErrorUri, Harvested) values (null, ?, ?, ?, ?, ?, ?, ?)
2018-04-14 11:57:56.769  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 6, relatedIdentifier=null, timestamp=1523703476769, payload=6]
2018-04-14 11:57:57.774  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 7, relatedIdentifier=null, timestamp=1523703477773, payload=7]
2018-04-14 11:57:58.778  INFO 4811 --- [pool-4-thread-1] o.i.c.e.util.producer.LogProducer        : GenericFlowEvent [identifier=Message 8, relatedIdentifier=null, timestamp=1523703478778, payload=8]
```
## Configuring Components
Currently we have hardcoded a value of 5 to indicate the bad message in the Converter. What if we want this to be user defined or even dynamic based on runtime?
Well we can tell a componenet that it is a ConfiguredResource. Telling it this allows access to configure attributes on the component through the Console.
Update the Converter component to implement ```ConfiguredResource``` and specify a Configuration class defining the allowed attributes.
```
package com.ikasan.example.converter;

public class MyConverterConfiguration
{
    public int badNumber = 5;

    public int getBadNumber() {
        return badNumber;
    }

    public void setBadNumber(int badNumber) {
        this.badNumber = badNumber;
    }
}
```

```
package com.ikasan.example.converter;

import org.ikasan.spec.component.transformation.Converter;
import org.ikasan.spec.component.transformation.TransformationException;
import org.ikasan.spec.configuration.ConfiguredResource;

public class MyConverter implements Converter<String,Integer>, ConfiguredResource<MyConverterConfiguration>
{
    String configuredResourceId;
    MyConverterConfiguration configuration;
    
    public Integer convert(String payload) throws TransformationException
    {
        String[] strings = payload.split(" ");
        int intPart = Integer.valueOf( strings[1] );
        if(intPart == configuration.getBadNumber())
        {
            throw new TransformationException("error - bad number received [" + configuration.getBadNumber() + "]");
        }
        return Integer.valueOf(intPart);
    }

    public String getConfiguredResourceId() {
        return configuredResourceId;
    }

    public void setConfiguredResourceId(String configuredResourceId) {
        this.configuredResourceId = configuredResourceId;
    }

    public MyConverterConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MyConverterConfiguration configuration) {
        this.configuration = configuration;
    }
}
```

Build it, run it, and open the Console from a Browser.

# Document Info

| Authors | Ikasan Development Team |
| --- | --- |
| Contributors | n/a |
| Date | March 2018 |
| Email | info@ikasan.org |
| WebSite | [http://www.ikasan.org](http://www.ikasan.org/) |