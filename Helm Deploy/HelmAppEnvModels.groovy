/*

CloudBees CD DSL: Application model for Helm chart deployment
- Depends on Helm.groovy
- EC-Helm and config "Helm"	

Instructions
- Turn off Smart Deploy, Check Dependency and Artifact Staging

TODO
- [] Get image tag from Artifactory
- [] Pass in chart and release name, halkeye/freshrss
- [] Create process to remove inventory items
		getEnvironmentInventoryItems(projectName: "Tri-test", environmentName: "qa").each { 
			deleteEnvironmentInventoryItem it.projectName, it.environmentName, it.applicationName, it.componentName, it.resourceName
		}

*/

def Project = 'HelmDeploy'
def AppName = "RSS"
def BaseNamespace = AppName.toLowerCase()
def Chart = 'halkeye/freshrss'
def Release = 'freshrss'

//def HelmHostName = 'agent-flow-agents'
def HelmHostName = 'kubectl-agent-flow-agents'

project Project,{
	environment 'qa',{
		namespace = "${BaseNamespace}-${environmentName}"
		environmentTier 'App Tier',{
			resource "${projectName}_${environmentName}_App Tier", hostName: HelmHostName
		}
	}
	application AppName, {
		applicationTier 'App Tier', {
			component 'HelmChart', {
				pluginKey = 'EC-Artifact'
				process 'Install', {
					processType = 'DEPLOY'
					processStep 'Create Artifact Placeholder', {
						actualParameter = [
							commandToRun: '''artifact artifactKey: "$[/myComponent/ec_content_details/artifactName]", groupId: "group"''',
							shellToUse: 'ectool evalDsl --dslFile',
						]
						processStepType = 'command'
						subprocedure = 'RunCommand'
						subproject = '/plugins/EC-Core/project'
					}
					processStep 'Helm fetch', {
						actualParameter = [
							'actionOnError': '0',
							'arguments': 'add ',
							'command': 'repo',
							'config': 'Helm',
							'errorValue': '',
							'options': '''\
								halkeye
								https://halkeye.github.io/helm-charts/
							'''.stripIndent(),
							'resultPropertySheet': '/myJob/runCustomCommand',
						]
						processStepType = 'plugin'
						subprocedure = 'Run Custom Command'
						subproject = '/plugins/EC-Helm/project'
					}
					processStep 'Helm repo update', {
						actualParameter = [
							'actionOnError': '0',
							'arguments': 'update',
							'command': 'repo',
							'config': 'Helm',
							'errorValue': '',
							'options': '',
							'resultPropertySheet': '/myJob/runCustomCommand',
						]
						processStepType = 'plugin'
						subprocedure = 'Run Custom Command'
						subproject = '/plugins/EC-Helm/project'
						useUtilityResource = '0'
					}
					processStep 'Helm Install', {
						actualParameter = [
							'chart': Chart,
							'config': 'Helm',
							'options': '''\
								--install
								-n=$[/myEnvironment/namespace]
								--create-namespace
								--set=ingress.enabled=true
								--set=ingress.hosts[0]=$[/myEnvironment/namespace].cb-demos.io
							'''.stripIndent(),
							'releaseName': Release,
							'resultPropertySheet': '/myJob/upgradeRelease',
						]
						processStepType = 'plugin'
						subprocedure = 'Upgrade Release'
						subproject = '/plugins/EC-Helm/project'
					}
					processStep 'Helm rollback', {
						actualParameter = [
							'config': 'Helm',
							'options': '',
							'releaseName': Release,
							'resultPropertySheet': '/myJob/rollbackRelease',
							'revisionNumber': '0',
						]
						processStepType = 'plugin'
						subprocedure = 'Rollback Release'
						subproject = '/plugins/EC-Helm/project'
					}
					processDependency 'Create Artifact Placeholder', targetProcessStepName: 'Helm fetch', {
						branchType = 'ALWAYS'
					}
					processDependency 'Helm Install', targetProcessStepName: 'Helm rollback', {
						branchType = 'ERROR'
					}
					processDependency 'Helm fetch', targetProcessStepName: 'Helm repo update', {
						branchType = 'ALWAYS'
					}
					processDependency 'Helm repo update', targetProcessStepName: 'Helm Install', {
						branchType = 'ALWAYS'
					}
				}

				// Custom properties

				property 'ec_content_details', {
					property 'artifactName', value: 'HelmCart'
					property 'pluginProjectName', value: 'EC-Artifact'
					property 'versionRange', value: 'TBD'
				}
			}
		}

		process 'Deploy', {
			processType = 'OTHER'
			processStep 'HelmChart', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'HelmChart'
				subcomponentApplicationName = applicationName
				subcomponentProcess = 'Install'
			}
			processStep 'Update Inventory', {
				applicationTierName = 'App Tier'
				processStepType = 'procedure'
				subprocedure = 'Push inventory'
				subproject = 'Helm'
			}
			processDependency 'HelmChart', targetProcessStepName: 'Update Inventory', {
				branchType = 'ALWAYS'
			}
		}
		process 'Undeploy', {
			processType = 'OTHER'
			processStep 'HelmChart', {
				applicationTierName = 'App Tier'
				processStepType = 'process'
				subcomponent = 'HelmChart'
				subcomponentApplicationName = applicationName
				subcomponentProcess = 'Uninstall'
			}
		}
		tierMap 'qa', {
			environmentName = 'qa'
			environmentProjectName = projectName
			tierMapping 'App Tier_qa', {
				applicationTierName = 'App Tier'
				environmentTierName = 'App Tier'
			}
		}
	}
}