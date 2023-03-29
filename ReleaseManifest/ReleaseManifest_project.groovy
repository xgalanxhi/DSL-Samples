project 'ReleaseManifest', {
	tracked = '1'

	component 'Shared Component', {
		pluginKey = 'EC-Artifact'

		formalParameter 'artifact', defaultValue: '', {
			orderIndex = '1'
			required = '1'
			type = 'entry'
		}

		formalParameter 'version', defaultValue: '', {
			orderIndex = '2'
			required = '1'
			type = 'entry'
		}

		process 'Install', {
			processType = 'DEPLOY'
			processStep 'Retrieve', {
				actualParameter = [
					'artifactName': '$[/myComponent/ec_content_details/artifactName]',
					'artifactVersionLocationProperty': '$[/myComponent/ec_content_details/artifactVersionLocationProperty]',
					'filterList': '$[/myComponent/ec_content_details/filterList]',
					'overwrite': '$[/myComponent/ec_content_details/overwrite]',
					'retrieveToDirectory': '$[/myComponent/ec_content_details/retrieveToDirectory]',
					'versionRange': '$[/myJob/ec_Shared Component-version]',
				]
				dependencyJoinType = 'and'
				processStepType = 'component'
				subprocedure = 'Retrieve'
				subproject = '/plugins/EC-Artifact/project'
			}
		}

		// Custom properties

		property 'ec_content_details', {

			// Custom properties

			property 'artifactName', value: '$[artifact]'
			artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
			filterList = ''

			property 'overwrite', value: 'update'
			pluginProcedure = 'Retrieve'

			property 'pluginProjectName', value: 'EC-Artifact'
			retrieveToDirectory = ''

			property 'versionRange', value: '$[version]'
		}
	}

	environment 'Prod', {
		environmentTier 'Tier 1', {
			resourceName = [
				"Release Manifest Prod",
			]
		}
	}

	environment 'QA', {
		environmentTier 'Tier 1', {
			resourceName = [
				"Release Manifest QA",
			]
		}
	}

	application 'Sample', {

		applicationTier 'Tier 1', {
			component 'Comp2', {
				actualParameter = [
					'artifact': 'com.example:comp2',
					'version': 'latest',
				]
				reference = '1'
				sourceComponentName = 'Shared Component'
				sourceProjectName = projectName
			}

			component 'Comp1', {
				actualParameter = [
					'artifact': 'com.example:comp1',
					'version': 'latest',
				]
				reference = '1'
				sourceComponentName = 'Shared Component'
				sourceProjectName = projectName
			}
		}

		process 'Deploy', {
			processType = 'DEPLOY'

			processStep 'Comp1', {
				applicationTierName = 'Tier 1'
				dependencyJoinType = 'and'
				processStepType = 'process'
				subcomponent = 'Comp1'
				subcomponentApplicationName = applicationName
				subcomponentProcess = 'Install'
			}

			processStep 'Comp2', {
				applicationTierName = 'Tier 1'
				dependencyJoinType = 'and'
				processStepType = 'process'
				subcomponent = 'Comp2'
				subcomponentApplicationName = applicationName
				subcomponentProcess = 'Install'
			}
		}

		tierMap 'Sample-Prod', {
			environmentName = 'Prod'
			environmentProjectName = projectName
			tierMapping 'Tier 1', {
				applicationTierName = 'Tier 1'
				environmentTierName = 'Tier 1'
			}
		}

		tierMap 'Sample-QA', {
			environmentName = 'QA'
			environmentProjectName = projectName
			tierMapping 'Tier 1', {
				applicationTierName = 'Tier 1'
				environmentTierName = 'Tier 1'
			}
		}	
	}

	pipeline 'App Pipeline', {

		formalParameter 'Snap', defaultValue: '', {
			orderIndex = '1'
			required = '1'
			type = 'entry'
		}

		formalParameter 'Comp1', defaultValue: '', {
			orderIndex = '2'
			required = '1'
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def artName = "com.example:comp1"
					def allArts = []
					getArtifacts().each { allArts.push(it.artifactName)}
					options.add("latest","latest")
					if (artName in allArts) {
						getArtifactVersions(artifactName: artName).each { art ->
							options.add(art.version, art.version)
						}
					}

					return options
					'''.stripIndent()
				type = 'select'
		}

		formalParameter 'Comp2', defaultValue: '', {
			orderIndex = '3'
			required = '1'
			optionsDsl = '''\
				import com.electriccloud.domain.FormalParameterOptionsResult
				def options = new FormalParameterOptionsResult()
				def artName = "com.example:comp1"
				def allArts = []
				getArtifacts().each { allArts.push(it.artifactName)}
				options.add("latest","latest")
				if (artName in allArts) {
					getArtifactVersions(artifactName: artName).each { art ->
						options.add(art.version, art.version)
					}
				}

				return options
				'''.stripIndent()
			type = 'select'
		}

		stage 'Configure', {
			colorCode = '#2ca02c'
			task 'Create Snapshot from input parameters', {
				command = """\
					ectool setPipelineRunName "\$[snap]" --flowRuntimeId "\$[/myPipelineRuntime/flowRuntimeId]"
					ectool setProperty "/projects/${projectName}/snap" --value "\$[snap]"
					ectool createSnapshot --projectName "${projectName}" --snapshotName "\$[snap]" --applicationName "Sample" --componentVersion "ec_Comp1-version"=\$[Comp1] "ec_Comp2-version"=\$[Comp2]
					""".stripIndent()
				taskType = 'COMMAND'
			}
		}

		stage 'QA', {
			colorCode = '#289ce1'

			task 'Deploy', {
				actualParameter = [
					'ec_enforceDependencies': '0',
					'ec_smartDeployOption': '0',
					'ec_stageArtifacts': '0',
				]
				environmentName = 'QA'
				environmentProjectName = projectName
				rollingDeployEnabled = '0'
				snapshotName = "\$[/projects/${projectName}/snap]"
				subapplication = 'Sample'
				subprocess = 'Deploy'
				subproject = projectName
				taskProcessType = 'APPLICATION'
				taskType = 'PROCESS'
			}
		}

		stage 'Production', {
			colorCode = '#ff7f0e'
			gate 'PRE', {
				task 'Approval', {
					gateType = 'PRE'
					notificationEnabled = '1'
					notificationTemplate = 'ec_default_gate_task_notification_template'
					subproject = projectName
					taskType = 'APPROVAL'
					approver = [
						'Everyone',
					]
				}
			}

			task 'Deploy', {
				actualParameter = [
					'ec_enforceDependencies': '0',
					'ec_smartDeployOption': '0',
					'ec_stageArtifacts': '0',
				]
				environmentName = 'Prod'
				environmentProjectName = projectName
				rollingDeployEnabled = '0'
				snapshotName = "\$[/projects/${projectName}/snap]"
				subapplication = 'Sample'
				subprocess = 'Deploy'
				subproject = projectName
				taskProcessType = 'APPLICATION'
				taskType = 'PROCESS'
			}
		}
	}

	release 'App Release', {

		pipeline 'App Pipeline', {
			releaseName = 'App Release'
			templatePipelineName = 'App Release Pipeline'
			templatePipelineProjectName = projectName

			formalParameter 'Snap', defaultValue: '', {
				orderIndex = '1'
				required = '1'
				type = 'entry'
			}

			formalParameter 'Comp1', defaultValue: '', {
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def artName = "com.example:comp1"
					def allArts = []
					getArtifacts().each { allArts.push(it.artifactName)}
					options.add("latest","latest")
					if (artName in allArts) {
						getArtifactVersions(artifactName: artName).each { art ->
							options.add(art.version, art.version)
						}
					}

					return options
					'''.stripIndent()
				orderIndex = '2'
				required = '1'
				type = 'select'
			}

			formalParameter 'Comp2', defaultValue: '', {
				optionsDsl = '''\
					import com.electriccloud.domain.FormalParameterOptionsResult
					def options = new FormalParameterOptionsResult()
					def artName = "com.example:comp2"
					def allArts = []
					getArtifacts().each { allArts.push(it.artifactName)}
					options.add("latest","latest")
					if (artName in allArts) {
						getArtifactVersions(artifactName: artName).each { art ->
							options.add(art.version, art.version)
						}
					}
					return options
					'''.stripIndent()
				orderIndex = '3'
				required = '1'
				type = 'select'
			}

			stage 'Configure', {
				colorCode = '#2ca02c'
				pipelineName = 'App Pipeline'

				task 'Create Snapshot from input parameters', {
					command = """\
						ectool setPipelineRunName "\$[snap]" --flowRuntimeId "\$[/myPipelineRuntime/flowRuntimeId]"
						ectool setProperty "/projects/${projectName}/Sample_snapshot" --value "\$[snap]"
						ectool createSnapshot --projectName "${projectName}" --snapshotName "\$[snap]" --applicationName "Sample" --componentVersion "ec_Comp1-version"=\$[Comp1] "ec_Comp2-version"=\$[Comp2]
						""".stripIndent()
					taskType = 'COMMAND'
				}
			}

			stage 'QA', {
				colorCode = '#289ce1'

				task 'Deploy', {
					deployerRunType = 'serial'
					subproject = projectName
					taskType = 'DEPLOYER'
				}
			}

			stage 'Production', {
				colorCode = '#ff7f0e'

				gate 'PRE', {

					task 'Approval', {
						gateType = 'PRE'
						notificationEnabled = '1'
						notificationTemplate = 'ec_default_gate_task_notification_template'
						subproject = projectName
						taskType = 'APPROVAL'
						approver = [
							'Everyone',
						]
					}
				}

				task 'Deploy', {
					deployerRunType = 'serial'
					subproject = projectName
					taskType = 'DEPLOYER'
				}
			}
		}
		deployerApplication 'Sample', {
			orderIndex = '1'
			processName = 'Deploy'
			smartDeploy = '0'

			deployerConfiguration 'Sample-QA', {
				deployerTaskName = 'Deploy'
				environmentName = 'QA'
				processName = 'Deploy'
				snapshotName = "\$[/projects/${projectName}/Sample_snapshot]"
				stageName = 'QA'
			}

			deployerConfiguration 'Sample-Prod', {
				deployerTaskName = 'Deploy'
				environmentName = 'Prod'
				processName = 'Deploy'
				snapshotName = "\$[/projects/${projectName}/Sample_snapshot]"
				stageName = 'Production'
			}
		}
	}
}
