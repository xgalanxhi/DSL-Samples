/*
CloudBees CD DSL: Push environment inventory based on Helm deployment

The procedure "Push Inventory" is intended to be run from the application deploy process in the 'App Tier'.
- Assumes the Helm chart component name is 'HelmChart'
- Assumes the name space is at /myEnvironment/namespace

- Finds release details using 'helm list'
- Finds deployment details using 'kubectl get deployment'
- Adds components to application model so that inventory can be pushed
- Push inventory: helm chart, app_version, component images
- Create snapshop based on app_version

TODO
- [x] HelmChart inventory not getting updated, still at 1.0. Lloks like app process is setting this after the procedure step does seeds the inventory. So, perhaps its possible to change the value that's going to be used for inventory.
- [x] Component definition missing version
- [] Rollback
- [] Inventory item completion times are not updated
- [] Environments not listed in Snapshot
- [x] Snapshot missing components. Snapshot is probably based on the app revision being run, not the modified one.

*/

def kubectl = '/home/cbflow/google-cloud-sdk/bin/kubectl'

project "Helm",{
	procedure "Push inventory",{
		step "Get Chart and Application Versions", shell: "ec-groovy", condition: true, command: '''\
			import groovy.json.JsonSlurper
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			def sout = new StringBuilder(), serr = new StringBuilder()
			def proc = ['helm','list', '-n', '$[/myEnvironment/namespace]', '-o', 'json'].execute()
			proc.consumeProcessOutput(sout, serr)
			proc.waitForOrKill(60000) // Give enough time for the shell command to return
			//println "out> $sout err> $serr"
			def jsonSlurper = new JsonSlurper()
			def object = jsonSlurper.parseText(sout.toString())
			
			println "Creating ChartVersion property: ${object[0].chart}"
			ef.setProperty propertyName: "/myJob/ChartVersion", value: object[0].chart
			println "Creating AppVersion property: ${object[0].app_version}"
			ef.setProperty propertyName: "/myJob/AppVersion", value: object[0].app_version
			println "Creating Deployment property: ${object[0].name}"
			ef.setProperty propertyName: "/myJob/Deployment", value: object[0].name
		'''.stripIndent()
		step "Get Component Versions", shell: "ec-groovy '{0}' ${kubectl}", command: '''\
			import groovy.json.JsonSlurper
			import groovy.json.JsonOutput
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			def sout = new StringBuilder(), serr = new StringBuilder()
			def proc = [this.args[0],'get', 'deployment', '$[/myJob/Deployment]', '-n', '$[/myEnvironment/namespace]', '-o', 'json'].execute()
			proc.consumeProcessOutput(sout, serr)
			proc.waitForOrKill(60000) // Give enough time for the shell command to return
			println "out> $sout err> $serr"
			def jsonSlurper = new JsonSlurper()
			def object = jsonSlurper.parseText(sout.toString())
			
			def Components = []
			def Containers = object.spec.template.spec.containers
			Containers.each { Container ->
				println "Found conainer ${Container.name} with image ${Container.image}"
				Components.push([name: Container.name, image: Container.image])
			}
			ef.setProperty propertyName: "/myJob/Components", value: JsonOutput.toJson(Components)
		'''.stripIndent()
		step "Set Chart Inventory", shell: "ec-groovy", condition: true, command: '''\
			import groovy.json.JsonSlurper
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			def ChartVersion = ef.getProperty(propertyName: "/myJob/ChartVersion").property.value
			println "Creating and environment inventory items for HelmChart with version ${ChartVersion}"
			ef.seedEnvironmentInventory(
				projectName: '$[/myApplication/projectName]',
				environmentName: '$[/myEnvironment]',
				applicationName: '$[/myApplication]',
				applicationProcessName: 'Deploy',
				status: 'success',
				applicationTierName: 'App Tier',
				artifactName: "HelmChart",
				artifactVersion: ChartVersion,
				componentName: "HelmChart",
				environmentProjectName: '$[/myApplication/projectName]',
				resourceNames: ['$[/myApplication/projectName]_$[/myEnvironment]_App Tier']
			)
			ef.setProperty propertyName: '/myApplication/components/HelmChart/ec_content_details/versionRange', value: ChartVersion
		'''.stripIndent()
		step "Add Components to Application Model", shell: "ectool evalDsl --dslFile", command: '''\
			import groovy.json.JsonSlurper
			def ComponentsJson = getProperty(propertyName: "/myJob/Components").value
			def jsonSlurper = new JsonSlurper()
			def Components = jsonSlurper.parseText(ComponentsJson)
			Components.each { Comp ->
				println "Adding component ${Comp.name} to Application model"
				project "$[/myApplication/projectName]",{
					application '$[/myApplication]', {
						applicationTier 'App Tier', {
							component Comp.name, pluginName: null, {
								pluginKey = 'EC-Artifact'
								property 'ec_content_details', {
									property 'artifactName', value: Comp.name
									property 'versionRange', value: Comp.image
								}
								process 'Install', {
									applicationName = null
									processType = 'DEPLOY'
								}
							}
						}
						process 'Deploy', {
							processStep Comp.name, {
								applicationTierName = 'App Tier'
								processStepType = 'process'
								subcomponent = Comp.name
								subcomponentApplicationName = applicationName
								subcomponentProcess = 'Install'
							}
							processDependency 'Update Inventory', targetProcessStepName: Comp.name, {
								branchCondition = 'false'
								branchConditionName = 'Never run'
								branchConditionType = 'CUSTOM'
								branchType = 'ALWAYS'
							}
						}
					}
				}				
			}			
		'''.stripIndent()
		step "Set Components Inventory", shell: "ec-groovy", condition: true, command: '''\
			import groovy.json.JsonSlurper
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			def ComponentsJson = ef.getProperty(propertyName: "/myJob/Components").property.value
			def jsonSlurper = new JsonSlurper()
			def Components = jsonSlurper.parseText(ComponentsJson)
			Components.each { Component ->
				println "Creating and environment inventory items for ${Component.name} with image ${Component.image}"
				ef.seedEnvironmentInventory(
					projectName: '$[/myApplication/projectName]',
					environmentName: '$[/myEnvironment]',
					applicationName: '$[/myApplication]',
					applicationProcessName: 'Deploy',
					status: 'success',
					applicationTierName: 'App Tier',
					artifactName: Component.name,
					artifactVersion: Component.image,
					componentName: Component.name,
					environmentProjectName: '$[/myApplication/projectName]',
					resourceNames: ['$[/myApplication/projectName]_$[/myEnvironment]_App Tier']
				)
			}
		'''.stripIndent()
		step "Create Application Snapshot", shell: "ectool evalDsl --dslFile", command: '''\
			def AppVersion = getProperty(propertyName: "/myJob/AppVersion").value
			snapshot snapshotName: AppVersion, projectName: "$[/myApplication/projectName]", applicationName: "$[/myApplication]"
		'''.stripIndent()
	}
}