Maven has served us well as a build tool and dependency manager, however as a tool it has not kept up with our rapidly evolving development processes. For example, when we switched to Git we were no longer able to use the maven-release-plugin because while that plugin is supposed to support Git it makes several assumptions about the release process which are not valid for everyones release process. Additionally, using Maven requires you to follow a specific set of conventions and you can't really go outside of those conventions. Gradle is a newer build tool which is supposed to be more powerful than Maven and which should allow us to customize the build process.

To use this plugin in your build, first you must define a file in '~/.gradle/' called 'init.gradle'.
```
allprojects {
    // Configure Maven Repositories to Lookup Dependencies
    ext.RepositoryConfiguration = {
        mavenLocal()
        maven {
            url = uri('release-repository-url')
        }
        maven {
            url = uri('snapshot-repository-url')
        }
    }
    buildscript.repositories RepositoryConfiguration
    repositories RepositoryConfiguration
  
    // Configure
    ext.releaseRepositoryDeployer = [
        url: "release-repository-url",
        userName: 'release-username',
        password: '*******'
    ]
    ext.snapshotRepositoryDeployer = [
        url: "snapshot-repository-url",
        userName: 'snapshot-username',
        password: '*******'
    ]
}
```
Configure your project information in your gradle.properties
```
group=theGroupId
artifact=theArtifactName
version=1.0.0-SNAPSHOT
name=A human friendly name
description=Some description of the artifact
Next you need to configure the plugin in your build.gradle.
```
```
buildscript {
	dependencies {
		classpath 'theGroupId:theArtifactName:1.0.0-SNAPSHOT'
	}
}

apply plugin: 'theArtifactName'
```
Finally, add relevant dependencies in 'build.gradle'. In this example, we first include the spring-cloud-starter-parent for spring-cloud versions before configuring dependencies.
```
build.gradle
dependencyManagement {
	imports {
		mavenBom 'org.springframework.cloud:spring-cloud-starter-parent:Brixton.M4'
	}
	dependencies {
		dependency 'com.gemstone.gemfire:gemfire:7.0.2'
	}
}

configurations {
	all*.exclude module: 'spring-boot-starter-logging'
}

dependencies {

	// Spring Boot
	compile 'org.springframework.boot:spring-boot-starter'
	providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
	compile 'org.springframework.boot:spring-boot-starter-actuator'
	compile 'org.springframework.boot:spring-boot-starter-log4j'
	providedRuntime 'org.slf4j:slf4j-api'
	providedRuntime 'org.slf4j:slf4j-log4j12'
	providedRuntime 'org.slf4j:jcl-over-slf4j'
	providedRuntime 'log4j:log4j'

	...

	testCompile 'org.springframework.boot:spring-boot-starter-test'
	testCompile 'org.springframework.security:spring-security-test'
}
```
```
gradle bootRun // Run the application
gradle test // Execute tests
gradle releaseGitPush // Package and deploy a release (requires correct repository credentials in ~/.gradle/init.gradle)
```