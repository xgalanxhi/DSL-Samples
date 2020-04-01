/*

Cloudbees Continuous Delivery DSL: Multi-Application Release Modeling with dynamic runtime pipelines

*/

def Project = 'Multi-App Release Subpipelines'


project Project, {

	component 'NoArtifact', {
		pluginKey = 'EC-Artifact'

		process 'Attach to Release', {
			processType = 'OTHER'
			smartUndeployEnabled = '0'

			processStep 'Start and attach pipeline', {
				actualParameter = [
					'commandToRun': '''\
						import com.electriccloud.client.groovy.ElectricFlow
						ElectricFlow ef = new ElectricFlow()
						def Project=ef.getProperty(propertyName: "/myJob/projectName", jobStepId: System.getenv()["COMMANDER_JOBSTEPID"]).property.value
						println "Project: ${Project}"
						// Workaround because /myComponent/componentName is returning the master component definition name
						def Pipeline=ef.getProperty(propertyName: "/myParent/parent/parent/subcomponent", jobStepId: System.getenv()["COMMANDER_JOBSTEPID"]).property.value
						println "Pipeline: ${Pipeline}"

						def Release="Multi-App with subpipelines"

						// Start Application Pipeline
						def PipelineRun = ef.runPipeline(projectName: Project, pipelineName: Pipeline).flowRuntime
						println PipelineRun.flowRuntimeId

						// Attach to Release
						ef.attachPipelineRun(projectName: Project, flowRuntimeId: PipelineRun.flowRuntimeId, releaseName: Release)
					'''.stripIndent(),
					'shellToUse': 'ec-groovy',
				]
				dependencyJoinType = 'and'
				processStepType = 'command'
				subprocedure = 'RunCommand'
				subproject = '/plugins/EC-Core/project'
			}
		}

		process 'Install', {
			processType = 'DEPLOY'

			processStep 'Create Artifact Placeholder', {
				actualParameter = [
					'commandToRun': '''\
						artifact artifactKey: "$[/myComponent/ec_content_details/artifactName]", groupId: "group"
					'''.stripIndent(),
					'shellToUse': 'ectool evalDsl --dslFile',
				]
				processStepType = 'command'
				subprocedure = 'RunCommand'
				subproject = '/plugins/EC-Core/project'
			}
		}

		process 'Uninstall', {
			processType = 'UNDEPLOY'

			processStep 'Uninstall', {
				actualParameter = [
					'commandToRun': 'echo "Uninstalling $[/myComponent]"',
				]
				processStepType = 'command'
				subprocedure = 'RunCommand'
				subproject = '/plugins/EC-Core/project'
			}
		}

		// Custom properties

		property 'ec_content_details', {

			// Custom properties

			property 'artifactName', value: '$[/myComponent/componentName]'
			artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
			filterList = ''

			property 'overwrite', value: 'update'
			pluginProcedure = 'Retrieve'

			property 'pluginProjectName', value: 'EC-Artifact'
			retrieveToDirectory = ''

			property 'versionRange', value: '$[comp1_version]'
		}
	}

	environment 'PROD', {

		environmentTier 'App Tier', {
			resource 'Multi-App Release Subpipelines_PROD_App Tier', hostName: getResource(resourceName: "local").hostName
		}
	}

	environment 'QA', {

		environmentTier 'App Tier', {
			resource 'Multi-App Release Subpipelines_QA_App Tier', hostName: getResource(resourceName: "local").hostName
		}
	}

	environment 'Utility', {
		reservationRequired = '0'

		environmentTier 'App Tier', {
			resource 'Multi-App Release Subpipelines_Utility_App Tier', hostName: getResource(resourceName: "local").hostName
		}
	}

	application 'AppA', {

		applicationTier 'App Tier', {
			applicationName = 'AppA'

			component 'comp1', {
				applicationName = 'AppA'
				reference = '1'
				sourceComponentName = 'NoArtifact'
				sourceProjectName = Project
			}

			component 'comp2', {
				applicationName = 'AppA'
				reference = '1'
				sourceComponentName = 'NoArtifact'
				sourceProjectName = Project
			}
		}

		process 'Attach to Release', {
			applicationName = 'AppA'
			exclusiveEnvironment = '0'
			processType = 'OTHER'

			formalParameter 'ec_comp1-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp1-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/components/NoArtifact/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_comp2-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp2-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/components/NoArtifact/ec_content_details/versionRange]', {
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

			processStep 'Attach comp1 pipeline', {
				applicationTierName = 'App Tier'
				dependencyJoinType = 'and'
				processStepType = 'process'
				subcomponent = 'comp1'
				subcomponentApplicationName = 'AppA'
				subcomponentProcess = 'Attach to Release'

				// Custom properties

				property 'ec_deploy', {

					// Custom properties
					ec_notifierStatus = '0'
				}
			}

			processStep 'Attach comp2 to Release', {
				applicationTierName = 'App Tier'
				dependencyJoinType = 'and'
				processStepType = 'process'
				subcomponent = 'comp2'
				subcomponentApplicationName = 'AppA'
				subcomponentProcess = 'Attach to Release'

				// Custom properties

				property 'ec_deploy', {

					// Custom properties
					ec_notifierStatus = '0'
				}
			}

			// Custom properties

			property 'ec_deploy', {

				// Custom properties
				ec_notifierStatus = '0'
			}
		}

		process 'Deploy', {
			applicationName = 'AppA'
			processType = 'OTHER'

			formalParameter 'comp1_version', defaultValue: '1.0'

			formalParameter 'comp2_version', defaultValue: '2.0'

			formalParameter 'ec_comp1-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp1-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/applications/AppA/components/comp1/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_comp2-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp2-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/applications/AppA/components/comp2/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_enforceDependencies', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_smartDeployOption', defaultValue: '0'

			formalParameter 'ec_stageArtifacts', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			processStep 'comp1', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'comp1'
				subcomponentApplicationName = 'AppA'
				subcomponentProcess = 'Install'

				// Custom properties

				property 'ec_deploy', {

					// Custom properties
					ec_notifierStatus = '0'
				}
			}

			processStep 'comp2', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'comp2'
				subcomponentApplicationName = 'AppA'
				subcomponentProcess = 'Install'


			}


		}

		process 'Undeploy', {
			applicationName = 'AppA'
			processType = 'OTHER'

			formalParameter 'ec_comp1-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp1-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/applications/AppA/components/comp1/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_comp2-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp2-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/applications/AppA/components/comp2/ec_content_details/versionRange]', {
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

			processStep 'comp1', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'comp1'
				subcomponentApplicationName = 'AppA'
				subcomponentProcess = 'Uninstall'


			}

			processStep 'comp2', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'comp2'
				subcomponentApplicationName = 'AppA'
				subcomponentProcess = 'Uninstall'


			}

		}

		tierMap 'Utility', {
			applicationName = 'AppA'
			environmentName = 'Utility'
			environmentProjectName = Project

			tierMapping 'App Tier_Utility', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}

		tierMap 'PROD', {
			applicationName = 'AppA'
			environmentName = 'PROD'
			environmentProjectName = Project

			tierMapping 'App Tier_PROD', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}

		tierMap 'QA', {
			applicationName = 'AppA'
			environmentName = 'QA'
			environmentProjectName = Project

			tierMapping 'App Tier_QA', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}

	}

	application 'AppB', {

		applicationTier 'App Tier', {
			applicationName = 'AppB'

			component 'comp3', {
				applicationName = 'AppB'
				reference = '1'
				sourceComponentName = 'NoArtifact'
				sourceProjectName = Project
			}
		}

		process 'Attach to Release', {
			applicationName = 'AppB'
			exclusiveEnvironment = '0'
			processType = 'OTHER'

			formalParameter 'ec_comp3-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp3-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/components/NoArtifact/ec_content_details/versionRange]', {
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

			processStep 'Attach comp3 pipeline', {
				applicationTierName = 'App Tier'
				dependencyJoinType = 'and'
				processStepType = 'process'
				subcomponent = 'comp3'
				subcomponentApplicationName = 'AppB'
				subcomponentProcess = 'Attach to Release'

			}

		}

		process 'Deploy', {
			applicationName = 'AppB'
			processType = 'OTHER'

			formalParameter 'comp3_version', defaultValue: '3.0'

			formalParameter 'ec_comp3-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp3-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/applications/AppB/components/comp3/ec_content_details/versionRange]', {
				expansionDeferred = '1'
				type = 'entry'
			}

			formalParameter 'ec_enforceDependencies', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_smartDeployOption', defaultValue: '0'

			formalParameter 'ec_stageArtifacts', defaultValue: '0', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			processStep 'comp3', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'comp3'
				subcomponentApplicationName = 'AppB'
				subcomponentProcess = 'Install'

			}

		}

		process 'Undeploy', {
			applicationName = 'AppB'
			processType = 'OTHER'

			formalParameter 'ec_comp3-run', defaultValue: '1', {
				expansionDeferred = '1'
				type = 'checkbox'
			}

			formalParameter 'ec_comp3-version', defaultValue: '$[/projects/Multi-App Release Subpipelines/applications/AppB/components/comp3/ec_content_details/versionRange]', {
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

			processStep 'comp3', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'comp3'
				subcomponentApplicationName = 'AppB'
				subcomponentProcess = 'Uninstall'

			}
		}

		tierMap 'Utility', {
			applicationName = 'AppB'
			environmentName = 'Utility'
			environmentProjectName = Project

			tierMapping 'App Tier_Utility', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}

		tierMap 'PROD', {
			applicationName = 'AppB'
			environmentName = 'PROD'
			environmentProjectName = Project

			tierMapping 'App Tier_PROD', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}

		tierMap 'QA', {
			applicationName = 'AppB'
			environmentName = 'QA'
			environmentProjectName = Project

			tierMapping 'App Tier_QA', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}

	}

	pipeline 'comp1', {

		formalParameter 'ec_stagesToRun', {
			expansionDeferred = '1'
		}

		stage 'Stage 1', {
			colorCode = '#00adee'
			pipelineName = 'comp1'

			gate 'PRE'

			gate 'POST'

			task 'Get Jira tickets', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}

			task 'Get Build Details', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}

			task 'Get Source Repo details', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}
	}

	pipeline 'comp2', {

		formalParameter 'ec_stagesToRun', {
			expansionDeferred = '1'
		}

		stage 'Stage 1', {
			colorCode = '#00adee'
			pipelineName = 'comp2'

			gate 'PRE'

			gate 'POST'

			task 'Get Jira tickets', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}

			task 'Get Build Details', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}

			task 'Get Source Repo details', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}

		// Custom properties

		property 'ec_counters', {

			// Custom properties
			pipelineCounter = '7'
		}
	}

	pipeline 'comp3', {

		formalParameter 'ec_stagesToRun', {
			expansionDeferred = '1'
		}

		stage 'Stage 1', {
			colorCode = '#00adee'
			pipelineName = 'comp3'

			gate 'PRE'

			gate 'POST'

			task 'Get Jira tickets', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}

			task 'Get Build Details', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}

			task 'Get Source Repo details', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}

		// Custom properties

		property 'ec_counters', {

			// Custom properties
			pipelineCounter = '7'
		}
	}

	release 'Multi-App with subpipelines', {
		plannedEndDate = '2020-04-15'
		plannedStartDate = '2020-04-01'

		pipeline 'pipeline_Multi-App with subpipelines', {
			releaseName = 'Multi-App with subpipelines'

			formalParameter 'ec_stagesToRun', {
				expansionDeferred = '1'
			}

			stage 'App Subpipelines', {
				colorCode = '#00adee'
				pipelineName = 'pipeline_Multi-App with subpipelines'

				gate 'PRE'

				gate 'POST'

				task 'Start Subpipelines', {
					deployerRunType = 'serial'
					subproject = Project
					taskType = 'DEPLOYER'
				}

				task 'Wait on subpipelines', {
					actualParameter = [
						'commandToRun': 'echo',
					]
					subpluginKey = 'EC-Core'
					subprocedure = 'RunCommand'
					taskType = 'COMMAND'

					waitDependency 'comp1', {
						dependentPipelineName = 'comp1'
						pipelineName = 'pipeline_Multi-App with subpipelines'
						releaseName = 'Multi-App with subpipelines'
						stageName = 'App Subpipelines'
						taskName = 'Wait on subpipelines'
					}

					waitDependency 'comp3', {
						dependentPipelineName = 'comp3'
						pipelineName = 'pipeline_Multi-App with subpipelines'
						releaseName = 'Multi-App with subpipelines'
						stageName = 'App Subpipelines'
						taskName = 'Wait on subpipelines'
					}

					waitDependency 'comp2', {
						dependentPipelineName = 'comp2'
						pipelineName = 'pipeline_Multi-App with subpipelines'
						releaseName = 'Multi-App with subpipelines'
						stageName = 'App Subpipelines'
						taskName = 'Wait on subpipelines'
					}
				}
			}

			stage 'Test', {
				colorCode = '#ff7f0e'
				pipelineName = 'pipeline_Multi-App with subpipelines'

				gate 'PRE'

				gate 'POST'

				task 'Deploy Apps', {
					deployerRunType = 'serial'
					subproject = Project
					taskType = 'DEPLOYER'
				}
			}

			stage 'Production', {
				colorCode = '#2ca02c'
				pipelineName = 'pipeline_Multi-App with subpipelines'

				gate 'PRE'

				gate 'POST'

				task 'Deploy Apps Copy', {
					deployerRunType = 'serial'
					subproject = Project
					taskType = 'DEPLOYER'
				}
			}
		}

		deployerApplication 'AppA', {
			enforceDependencies = '1'
			orderIndex = '1'
			processName = 'Deploy'

			deployerConfiguration 'AppA-Utility', {
				deployerTaskName = 'Start Subpipelines'
				environmentName = 'Utility'
				processName = 'Attach to Release'
				stageName = 'App Subpipelines'
			}

			deployerConfiguration 'AppA-QA', {
				deployerTaskName = 'Deploy Apps'
				environmentName = 'QA'
				processName = 'Deploy'
				stageName = 'Test'
				actualParameter 'comp1_version', '1.0'
				actualParameter 'comp2_version', '2.0'
			}

			deployerConfiguration 'AppA-PROD', {
				deployerTaskName = 'Deploy Apps Copy'
				environmentName = 'PROD'
				processName = 'Deploy'
				stageName = 'Production'
				actualParameter 'comp1_version', '1.0'
				actualParameter 'comp2_version', '2.0'
			}
		}

		deployerApplication 'AppB', {
			enforceDependencies = '1'
			orderIndex = '2'
			processName = 'Deploy'

			deployerConfiguration 'AppB-Utility', {
				deployerTaskName = 'Start Subpipelines'
				environmentName = 'Utility'
				processName = 'Attach to Release'
				stageName = 'App Subpipelines'
			}

			deployerConfiguration 'AppB-QA', {
				deployerTaskName = 'Deploy Apps'
				environmentName = 'QA'
				processName = 'Deploy'
				stageName = 'Test'
				actualParameter 'comp3_version', '3.0'
			}

			deployerConfiguration 'AppB-PROD', {
				deployerTaskName = 'Deploy Apps Copy'
				environmentName = 'PROD'
				processName = 'Deploy'
				stageName = 'Production'
				actualParameter 'comp3_version', '3.0'
			}
		}
	}
}
