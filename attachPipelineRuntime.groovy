/*

CloudBees CD DSL: Attach pipeline run to a release

When run from a pipeline task, this procedure will attach the pipeline
run to a specified release. It will also remove any pipeline runs that have the
the same definition name and project name as the pipeline run to be attached.
The ideas is that a release candidate pipeline may be run many times before the
release event. Only the last pipeline run for a given repo or component remains
attached for the release event as part of the release manifest.

*/

def Project = "Release Tools"

project Project,{

	procedure "Attach Pipeline to Release",{
		formalParameter "Release"
		formalParameter "ReleaseProject"
		
		step "Find Initiating Pipeline", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			def Release = '$[Release]'
			def ReleaseProject = '$[ReleaseProject]'
			def CurrentPipelineId = '$[/myPipelineRuntime/flowRuntimeId]'
			
			def getAncestor
			getAncestor = { id ->
				def Parent = ef.getProperty(propertyName: "/myTriggeringPipelineRuntime/flowRuntimeId", flowRuntimeId : id,
					/*Property Exists, this pipeline was triggered by another*/ { response, data ->
						println "Pipeline $id was triggered by ${data.property.value}"
						getAncestor(data.property.value)
					},
						/*Property does not exist, we have found the first ancestor*/ { response, data ->
						println "Pipeline $id is the first ancestor"
						return id
					}					
				
				)
			}
			
			def InitiatingPipelineId = getAncestor(CurrentPipelineId)
			
			def InitiatingPipelineName = ef.getProperty(propertyName:"/myPipelineRuntime/pipelineName", flowRuntimeId: InitiatingPipelineId).property.value
			println "Found initiating pipeline: ${InitiatingPipelineName} - ${InitiatingPipelineId}"
			ef.setProperty(propertyName: "/myJob/InitiatingPipelineId", value: InitiatingPipelineId)
			ef.setProperty(propertyName: "/myJob/InitiatingPipelineName", value: InitiatingPipelineName)
			
		'''.stripIndent()
		
		step "Detach any runtimes for this pipeline definition", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			def Release = '$[Release]'
			def ReleaseProject = '$[ReleaseProject]'
			def CurrentPipelineId = '$[/myPipelineRuntime/flowRuntimeId]'
			def InitiatingPipelineId = '$[InitiatingPipelineId]'
			def InitiatingPipelineName = '$[InitiatingPipelineName]'
			
			def PipelineIdToDetach = InitiatingPipelineId
			def PipelineRuntimeToDetach = ef.getProperty(propertyName:"/myPipelineRuntime/name", flowRuntimeId: InitiatingPipelineId).property.value
			def PipelineNameToDetach = ef.getProperty(propertyName:"/myPipelineRuntime/pipelineName", flowRuntimeId: InitiatingPipelineId).property.value
			def PipelineProjectToDetach = ef.getProperty(propertyName:"/myPipelineRuntime/projectName", flowRuntimeId: InitiatingPipelineId).property.value
			
			println "This pipeline run, ${PipelineRuntimeToDetach}: ${PipelineNameToDetach}, in project: ${PipelineProjectToDetach}"
			println "Looking for pipeline runs with this name and project to detach"
			def AttachedPipelines = ef.getAttachedPipelineRuns(projectName: ReleaseProject, releaseName : Release).attachedPipelineRunDetail
			AttachedPipelines.each { pipe ->
				def AttachedPipelineRuntime = ef.getProperty(propertyName:"/myPipelineRuntime/name",flowRuntimeId:pipe.flowRuntimeId).property.value
				def AttachedPipelineName = ef.getProperty(propertyName:"/myPipelineRuntime/pipelineName",flowRuntimeId:pipe.flowRuntimeId).property.value
				def AttachedProjectName = ef.getProperty(propertyName:"/myPipelineRuntime/projectName",flowRuntimeId:pipe.flowRuntimeId).property.value
				println "Found pipeline, ${AttachedPipelineRuntime}: ${AttachedPipelineName}, project: ${AttachedProjectName}"
				def AttachedPipelineId = ef.getProperty(propertyName:"/myPipelineRuntime/flowRuntimeId",flowRuntimeId:pipe.flowRuntimeId).property.value
				if (PipelineNameToDetach==AttachedPipelineName && PipelineProjectToDetach==AttachedProjectName) {
					println "Detaching pipeline, ${AttachedPipelineRuntime}: name: ${PipelineNameToDetach} project: ${AttachedProjectName}  id: ${AttachedPipelineId}"
					ef.detachPipelineRun(flowRuntimeId:AttachedPipelineId, projectName: ReleaseProject, releaseName : Release)
				}
			}
			
		'''.stripIndent()
		
		step "Attach this runtime pipeline", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			def Release = '$[Release]'
			def ReleaseProject = '$[ReleaseProject]'
			def CurrentPipelineId = '$[/myPipelineRuntime/flowRuntimeId]'
			def InitiatingPipelineId = '$[InitiatingPipelineId]'
			def InitiatingPipelineName = '$[InitiatingPipelineName]'
			
			println "Attaching pipeline: ${InitiatingPipelineName} - ${InitiatingPipelineId}"
			ef.attachPipelineRun(flowRuntimeId:InitiatingPipelineId, projectName: ReleaseProject, releaseName : Release)
		'''.stripIndent()
		
		step "Create link to release in Summary", command: '''\
			ectool setProperty "/myStageRuntime/ec_summary/Release Portfolio" "<html><a target='_blank' href='#pipeline-run-hierarchy/$[/projects[$[ReleaseProject]]/releases[$[Release]]/releaseId]/release-portfolio-list'>$[ReleaseProject] :: $[Release]</a></html>"
		'''.stripIndent()
		
		step "Create job link to release", command: '''\
			ectool setProperty "/myJob/report-urls/Release Portfolio" "../flow/#pipeline-run-hierarchy/$[/projects[$[ReleaseProject]]/releases[$[Release]]/releaseId]/release-portfolio-list"
		'''.stripIndent()

	} // procedure
} // project
