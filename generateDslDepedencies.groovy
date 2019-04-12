/* Generate DSL for objects and all its dependencies

Instruction
-----------
1. Edit the Releases, Pipelines, and/or Applications definitions below to point to one or more objects to be exported as DSL as well as their dependencies. Examine the the list below to see which dependency objects will be exported.
2. Run this DSL file. The output will contain the exported DSL for all the objects


What's been implemented and what's left to do
---------------------------------------------
Releases
-[x] Projects
-[x] Each self
-[x] Procedures
-[ ] Applications (process)
-[ ] Applications (deployer)
-[ ] Pipelines
-[ ] Releases
Pipelines
-[x] Projects
-[x] Each self
-[x] Procedures
-[ ] Applications (process)
-[ ] Applications (deployer)
-[ ] Pipelines
-[ ] Releases 
Applications
-[x] Projects
-[x] Each Self
-[x] Procedures (not from plugins)
	-[x] Projects (Application and Component process steps)
	-[ ] Subprocedures (recursive)
-[x] Master Components
	-[x] Projects (process steps)
	-[x] Procedures (not from plugins)
	-[ ] Subprocedures (recursive)
-[x] Environments
	-[x] Projects
	-[x] Resources
-[ ] Environment Templates
	-[ ] Projects
	-[ ] Dynamic Resources

Todo
- Microservices
- Artifacts
- Artifact Versions

*/

// User editable. These are arrays of lists, where the list needs to include projectName and one of
// releaseName, pipelineName, applicationName
def Releases = []
def Pipelines = []
def Applications = [[projectName: "SF", applicationName: "Store Front"]]

// Don't edit
def Projects = []
def Procedures = []
def MasterComponents = []
def Environments = []
def Resources = []
def ResourcePools = []

Releases.each { rel ->
	if (!(rel.projectName in Projects)) {Projects.push(rel.projectName)}
	def pipeName = getRelease(projectName: rel.projectName, releaseName: rel.releaseName).pipelineName
	getStages(projectName: rel.projectName, releaseName: rel.releaseName, pipelineName: pipeName).each { stage ->
		getTasks(projectName: rel.projectName, releaseName: rel.releaseName, pipelineName: pipeName, stageName: stage.stageName).each { task ->
			if ((String) task.taskType == 'PROCEDURE') {
				Procedures.push([projectName: task.subproject, procedureName: task.subprocedure])
			}
		}
	}
}

Pipelines.each { pipe ->
	if (!(pipe.projectName in Projects)) {Projects.push(pipe.projectName)}
	getStages(projectName: pipe.projectName, pipelineName: pipe.pipelineName).each { stage ->
		getTasks(projectName: pipe.projectName, pipelineName: pipe.pipelineName, stageName: stage.stageName).each { task ->
			if ((String) task.taskType == 'PROCEDURE') {
				Procedures.push([projectName: task.subproject, procedureName: task.subprocedure])
			}
		}
	}
}

Applications.each { app ->
	if (!(app.projectName in Projects)) {Projects.push(app.projectName)}
	// Get Environments
	getTierMaps(projectName: app.projectName, applicationName: app.applicationName).each { map ->
		if (!([projectName:map.environmentProjectName, environmentName: map.environmentName] in Environments)) {
			Environments.push([projectName:map.environmentProjectName, environmentName: map.environmentName])
		}
	}
	// Get procedures in application processes
	getProcesses(projectName: app.projectName, applicationName: app.applicationName).each { proc ->
		getProcessSteps(projectName: app.projectName, applicationName: app.applicationName, processName: proc.processName).each { step ->
			if (step?.subprocedure) {
				if (!step.subproject.contains("/plugins/")) {
					if (!(step.subproject in Projects)) {Projects.push(step.subproject)}
					if (!([projectName: step.subproject, procedureName: step.subprocedure] in Procedures)) {
						Procedures.push([projectName: step.subproject, procedureName: step.subprocedure])
					}
				}
			}
		}
	}
	// Get component procedures
	getComponents(projectName: app.projectName, applicationName: app.applicationName).each { comp ->
		getProcesses(projectName: comp.projectName, componentApplicationName: comp.applicationName, componentName: comp.componentName).each { proc ->
			getProcessSteps(projectName: comp.projectName, componentApplicationName: comp.applicationName, componentName: comp.componentName, processName: proc.processName).each { step ->
				if (step?.subprocedure) {
					if (!step.subproject.contains("/plugins/")) {
						if (!(step.subproject in Projects)) {Projects.push(step.subproject)}
						if (!([projectName: step.subproject, procedureName: step.subprocedure] in Procedures)) {
							Procedures.push([projectName: step.subproject, procedureName: step.subprocedure])
						}
					}
				}
			}
		}
		// Get Master Components
		if (comp?.refComponentName) {
			if (!(comp.refComponentProjectName in Projects)) {Projects.push(comp.refComponentProjectName)}
			if (!([projectName: comp.refComponentProjectName, componentName: comp.refComponentName] in MasterComponents)) {
				MasterComponents.push([projectName: comp.refComponentProjectName, componentName: comp.refComponentName])
			}
		}
	}
}

Environments.each { env ->
	if (!(env.projectName in Projects)) {Projects.push(env.projectName)}
	getEnvironmentTiers(projectName: env.projectName, environmentName: env.environmentName).each { tier ->
		getResourcesInEnvironmentTier(projectName: env.projectName, environmentName: env.environmentName,
			environmentTierName: tier.environmentTierName).each { res ->
			if (!(res.resourceName in Resources)) {Resources.push(res.resourceName)}
		}
		getResourcePoolsInEnvironmentTier(projectName: env.projectName, environmentName: env.environmentName,
			environmentTierName: tier.environmentTierName).each { pool ->
			def resourceList = []
			getResourcesInPool(resourcePoolName: pool.resourcePoolName).each { res ->
				if (!(res.resourceName in Resources)) {Resources.push(res.resourceName)}
				resourceList.push(res.resourceName)
			}
			if (!(pool.resourcePoolName in ResourcePools)) {ResourcePools.push([resourcePoolName: pool.resourcePoolName, resources: resourceList])}
		}
	}
}

// Workaround: getValue() below is introducing bogus first and last line. The following procedure strips these out.
def striptFirstLast = { input ->
	def stringInput = (String) input
	def lineArray = (ArrayList) stringInput.split('\n')
	lineArray.remove(lineArray.size()-1)
	lineArray.remove(0)
	return lineArray.join('\n') + '\n'
}

def dslOut = ""
transaction {

	Resources.each {
		dslOut += striptFirstLast(generateDsl(path: "/resources/${it}").getValue())
	}
	ResourcePools.each {
		dslOut += striptFirstLast(generateDsl(path: "/resourcePools/${it.resourcePoolName}").getValue())
	}
	Projects.each {
		dslOut += "project \"${it}\"" + '\n'
	}
	Procedures.each {
		dslOut += striptFirstLast(generateDsl(path: "/projects/${it.projectName}/procedures/${it.procedureName}").getValue())
	}
	MasterComponents.each {
		dslOut += striptFirstLast(generateDsl(path: "/projects/${it.projectName}/components/${it.componentName}").getValue())
	}
	Environments.each {
		dslOut += striptFirstLast(generateDsl(path: "/projects/${it.projectName}/environments/${it.environmentName}").getValue())
	}
	Applications.each {
		dslOut += striptFirstLast(generateDsl(path: "/projects/${it.projectName}/applications/${it.applicationName}").getValue())
	}
	Pipelines.each {
		dslOut += striptFirstLast(generateDsl(path: "/projects/${it.projectName}/pipelines/${it.pipelineName}").getValue())
	}
	Releases.each {
		dslOut += striptFirstLast(generateDsl(path: "/projects/${it.projectName}/releases/${it.releaseName}").getValue())
	}
}

dslOut