package com.build.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.ajoberstar.grgit.Grgit

class GradlePlugin implements Plugin<Project> {
	void apply(Project project) {
		project.plugins.apply 'spring-boot'
		project.plugins.apply 'propdeps'
		project.plugins.apply 'propdeps-maven'
		project.plugins.apply 'propdeps-idea'
		project.plugins.apply 'propdeps-eclipse'
		project.plugins.apply 'war'

		project.extensions.create('gpe', GradlePluginExtension)
		project.gpe.git = Grgit.open(dir: project.projectDir)

		project.processResources {
			from('src/main/resources') {
				exclude '**/*.html'
				expand(project.properties)
			}
			from('src/main/resources') {
				include '**/*.html'
			}
		}

		if (project.hasProperty('releaseRepositoryDeployer') || project.hasProperty('snapshotRepositoryDeployer')) {
			project.uploadArchives {
				repositories {
					mavenDeployer {
						if (project.hasProperty('releaseRepositoryDeployer')) {
							repository(url: project.releaseRepositoryDeployer.url) {
								authentication(
									userName: project.releaseRepositoryDeployer.userName,
									password: project.releaseRepositoryDeployer.password
								)
							}
						}
						if (project.hasProperty('snapshotRepositoryDeployer')) {
							snapshotRepository(url: project.snapshotRepositoryDeployer.url) {
								authentication(
									userName: project.snapshotRepositoryDeployer.userName,
									password: project.snapshotRepositoryDeployer.password
								)
							}
						}
					}
				}
			}
		}

		project.task('releaseIsSnapshot') {
			description = 'Check if the version is a -SNAPSHOT version'
			if (!project.version.contains('-SNAPSHOT')) {
				throw new Exception("Version must be a SNAPSHOT version when building a release (found $version), are you sure you are releasing the correct code?")
			}
		}

		project.task(dependsOn: ['releaseIsSnapshot', 'test'], 'releaseRemoveSnapshot') << {
			project.gpe.newVersion = project.version - '-SNAPSHOT'
		}

		project.task(dependsOn: ['releaseRemoveSnapshot'], 'releaseSetVersion') << {
			description = "Update version from ${project.version} => ${project.newVersion}"
			ant.propertyfile(file: 'gradle.properties') {
				entry(key: 'version', value: project.gpe.newVersion)
			}

			// Remove comment lines to prevent automatic timestamp
			ant.replaceregexp(file: 'gradle.properties', match: '^#.*\n', replace: '')
		}

		project.releaseSetVersion.onlyIf {
			project.gpe.hasProperty('newVersion')
		}

		project.task(dependsOn: 'releaseSetVersion', 'releaseUpdateVersion') << {
			project.gpe.git.add(patterns: ['gradle.properties'])
			project.gpe.git.commit(message: "[Build] Update $version to ${project.gpe.newVersion} for release")
			project.gpe.git.tag.add(name: "v$version")
		}

		project.task(dependsOn: ['releaseUpdateVersion', 'uploadArchives'], 'releaseNexusDeploy')

		project.task(dependsOn: 'releaseNexusDeploy', 'releaseGitPush') << {
			project.gpe.git.push()
			project.gpe.git.push(tags: true)
		}
	}
}