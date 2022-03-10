/*

Cloudbees CD/RO DSL: Create master component based application with snapshot and a release

Installation
1. Manually create artifact versions com.dslsamples:mastercomponent:1.0 and com.dslsamples:mastercomponent:1.0 (the example will run, but the deployment will fail because there is no file in the artifact version)
2. Run this DSL using the DSLIDE or command line:
	ectool evalDsl --dslFile MasterComponentSnapshots.groovy
	
What's created
- A master component with parameterized group and artifact name
- Application that instantiates two master components
- Snapshot that sets one component to version 1.0 and the other 2.0
- Two environment models and resources (each pointing to the local agent machine), qa and prod
- Release model with two stages, qa and prod, each with a DEPLOYER
	- Application selected and configured to run snapshot
	- Environment configurations for qa and prod stages

Instructions
1. Examine the application, snapshot and release 
2. Run the release model
3. Note the environment inventory

TODO:
- Remove dependency on transaction (to be deprecated). This may involve a product change.
- Publish dummy artifact versions

*/

def ArtifactGroup = "com.dslsamples"
def ArtifactName = "mastercomponent"
def ProjName = 'DSL-Samples'
def AppName = 'MC App'
def SnapName = "Rel1"
def CompStruct = [
	[componentName: "Comp1", group: ArtifactGroup, artifact: ArtifactName, version: "1.0"],
	[componentName: "Comp2", group: ArtifactGroup, artifact: ArtifactName, version: "2.0"],
]
def Envs = ["qa","prod"]
def RelName = "Snapshot app delivery"

// This only create the artifact version container. Files need to be uploaded for successful deployments
["1.0","2.0"].each { art ->
	artifactVersion artifactName: "${ArtifactGroup}:${ArtifactName}", version: art
}

// Create master component
project ProjName, {
	component 'MC', {
		pluginKey = 'EC-Artifact'
		formalParameter 'MC_group', defaultValue: 'TBD', {
			orderIndex = '1'
			required = '1'
			type = 'entry'
		}
		formalParameter 'MC_artifact', defaultValue: 'TBD', {
			orderIndex = '2'
			required = '1'
			type = 'entry'
		}
		process 'Install', {
			processType = 'DEPLOY'
			processStep 'Retrieve Artifact', {
				actualParameter = [
					artifactName: '$[/myComponent/ec_content_details/artifactName]',
					artifactVersionLocationProperty: '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
					filterList: '$[/myComponent/ec_content_details/filterList]',
					overwrite: '$[/myComponent/ec_content_details/overwrite]',
					retrieveToDirectory: '$[/myComponent/ec_content_details/retrieveToDirectory]',
					versionRange: '$[/myJob/ec_' + componentName + "${componentName}-version]",
				]
				dependencyJoinType = 'and'
				processStepType = 'component'
				subprocedure = 'Retrieve'
				subproject = '/plugins/EC-Artifact/project'
			}
		}
		property 'ec_content_details', {
			property 'artifactName', value: '$[MC_group]:$[MC_artifact]'
			artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
			filterList = ''
			property 'overwrite', value: 'update'
			pluginProcedure = 'Retrieve'
			property 'pluginProjectName', value: 'EC-Artifact'
			retrieveToDirectory = ''
			property 'versionRange', value: ''
		}
	}
}

// Create application
project ProjName, {
	CompStruct.each { Comp ->
		application AppName, {
			applicationTier 'Tier 1', {
				component Comp, {
					reference = '1'
					sourceComponentName = 'MC'
					sourceProjectName = projectName
					actualParameter = [
						MC_artifact: Comp.artifact,
						MC_group: Comp.group,
					]				
				}
			}
			process 'Deploy', {
				processType = 'DEPLOY'
				formalParameter "ec_${Comp.componentName}-run", defaultValue: '1', {
					expansionDeferred = '1'
					type = 'checkbox'
				}
				formalParameter "ec_${Comp.componentName}-version", defaultValue: "\$[/projects/${projectName}/components/${Comp.componentName}/ec_content_details/versionRange]", {
					expansionDeferred = '1'
					type = 'entry'
				}
				formalParameter 'ec_enforceDependencies', defaultValue: '0', {
					expansionDeferred = '1'
					type = 'checkbox'
				}
				formalParameter 'ec_smartDeployOption', defaultValue: '1', {
					expansionDeferred = '1'
					type = 'checkbox'
				}
				formalParameter 'ec_stageArtifacts', defaultValue: '0', {
					expansionDeferred = '1'
					type = 'checkbox'
				}
				processStep "${Comp.componentName} deploy", {
					applicationTierName = 'Tier 1'
					dependencyJoinType = 'and'
					processStepType = 'process'
					subcomponent = Comp.componentName
					subcomponentApplicationName = applicationName
					subcomponentProcess = 'Install'
				}
			}

			Envs.each { Env ->
				environment Env, {
					environmentTier 'Tier 1', {
						resource "${projectName}-${Env}", hostName: getResource(resourceName: "local").hostName
					}
					tierMap "$AppName-$Env", tierMapping: ['Tier 1':'Tier 1'], {
						environmentName = Env
						environmentProjectName = ProjName
				
					}
				}
			}
		}
	}
}

// Create snapshot and release pipeline
transaction {
	snapshot SnapName, projectName: ProjName, applicationName: AppName, {
		property 'ec_component_versions', {
			CompStruct.each { Comp ->
				property "ec_${Comp.componentName}", {
					property 'ec_component_processes', {
					}
					ec_Version = Comp.version
					ec_artifactName = "${Comp.group}:${Comp.artifact}"
					ec_deployed = 'false'
				}
			}
		}
	}

	project ProjName,{
		release RelName, {

			pipeline "pipeline_${releaseName}", {
				Envs.each { Env ->
					stage Env, {
						task 'Deploy', {
							deployerRunType = 'serial'
							subproject = projectName
							taskType = 'DEPLOYER'
						}
					}
					deployerApplication AppName, {
						enforceDependencies = '0'
						errorHandling = 'stopOnError'
						orderIndex = '1'
						processName = 'Deploy'
						smartDeploy = '1'
						snapshotName = SnapName
						stageArtifacts = '0'
						deployerConfiguration Env, {
							deployerTaskName = 'Deploy'
							environmentName = Env
							processName = 'Deploy'
							stageName = Env
						}
					}
				}
			}	
		}
	}
}
