To use this plugin in your build, first you must define a file in `~/.gradle/` called `init.gradle`.

```
allprojects {
	// Configure Nexus repositories for dependencies
	ext.RepositoryConfiguration = {
		maven {
			name = 'releases'
			url = uri('https://itnexus.garmin.com/nexus/content/groups/public')
		}
		maven {
			name = 'snapshots'
			url = uri('https://itnexus.garmin.com/nexus/content/groups/public-snapshots')
		}
		mavenLocal()
	}
	buildscript.repositories RepositoryConfiguration
	repositories RepositoryConfiguration

	// Configure Nexus repositories for artifact storage
	ext.releaseRepositoryDeployer = [
		url: "https://itnexus.garmin.com/nexus/content/repositories/releases",
		userName: 'it-snapshot',
		password: 'r3l3as3'
	]
	ext.snapshotRepositoryDeployer = [
		url: "https://itnexus.garmin.com/nexus/content/repositories/snapshots",
		userName: 'it-snapshot',
		password: 'garm1n'
	]
}
```

Configure your project information in your `gradle.properties`.

```
group=com.garmin.project
artifact=theArtifactName
version=1.0.0-SNAPSHOT
name=A human friendly name
description=Some description of the artifact

```

Next you need to configure the plugin in your `build.gradle`.

```
buildscript {
	dependencies {
		classpath 'com.garmin.build.gradle:garmin-gradle-plugin:1.0.0-SNAPSHOT'
	}
}

apply plugin: 'garmin-gradle-plugin'
```

Finally, add relevant dependencies in `build.gradle. In this example, we first include the spring-cloud-starter-parent for spring-cloud versions before configuring dependencies.

```
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
