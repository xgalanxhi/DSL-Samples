/*

Run procedure "CDRO Scripting" :: "Run CDRO Script"
"script" variable content can be edited

ectool deleteProcedure RuntimeOnly "Scripting2";ectool evalDsl --dslFile "ScriptToJob.groovy";ectool runProcedure "Scripting2" --procedureName "Run CDRO Script"

TODO:
- subprocedure step, extra hierarchy?
- Passing parameter to pipeline and other non-job entities
- Passing data between steps (output parameters, job properties?)
	- SSC has not output parameters, but the underlying procedure can. What about DSL-implemented SSC
*/

def TestProject = "Scripting2"

def script = """\
---
name: Sample CDRO script
run-name: Sample CDRO script
on: [push]
jobs:
  test-script:
   runs-on: ubuntu
   steps:
   - name: First Step of script
     project: ${TestProject}
     procedure: test
     with:
       input: 12345
   - name: Second Step of script
     project: ${TestProject}
     pipeline: test
     with:
       input: 12345
"""

project TestProject,{
	procedure "test",{
		formalParameter "input"
		step "echo", command: "echo testing \$[input]"
	}
	pipeline "test",{
		formalParameter "input"
	}
	procedure "Run CDRO Script",{
		formalParameter "Script", type: "textarea", defaultValue: script
		step "Parse and run script", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			import com.electriccloud.client.groovy.models.ActualParameter
			ElectricFlow ef = new ElectricFlow()
			import groovy.yaml.YamlSlurper
			def scriptYaml = \'\'\'\$[script]\'\'\'.stripIndent()
			def script = new YamlSlurper().parseText(scriptYaml)
			def cdroHost = '$[/server/settings/ipAddress]'
			def jobId = '$[/myJob/jobId]'
			def jobName = script["run-name"]+'_$[/increment /myProject/scriptRunCount]_$[/timestamp]'
			ef.modifyProperty(
				jobId: jobId,
				propertyName: "jobName",
				value: jobName
			)
			script.jobs.each { scriptName, scriptContent ->
				def scriptParentJobStep = ef.createJobStep(
					jobStepName : scriptName,
					jobId:jobId,
					parentPath: "/jobs/${jobName}",
					workspaceName: '$[/myWorkspace/workspaceName]'
				)				
				def resource = scriptContent["runs-on"]
				scriptContent.steps.each { step ->
					println "Step: ${step.name}"
					// Get step type
					def supportedObjects = ["procedure","pipeline"]
					def listOfargs = []
					step.each {k,v -> listOfargs.add(k)}
					def stepType = supportedObjects.intersect(listOfargs)[0] // ignore multiple
					println "Step type: ${stepType}"
					
					// Get actual parameters
					def args = []
					step.with.each { arg, value ->
						args.add (
							new ActualParameter(
								actualParameterName: arg,
								value: value)						
						)
					}
					
					if (stepType=="procedure") {
						def jobStepId = ef.createJobStep(
							jobStepName : step.name,
							subproject : step.project,
							subprocedure : step.procedure,
							actualParameters : args,
							jobId:jobId,
							parentPath: "/jobSteps/${scriptParentJobStep.jobStep.jobStepId}",
							resourceName: resource, 
							workspaceName: "$[/myWorkspace/workspaceName]"
						).jobStep.jobStepId
					}
					
					if (stepType=="pipeline") {
						// Using EC-Core::runCommand as a workaround because "command:" throwing NO_SESSION
						def pipelineId // Needed for preprocessor
						def flowRuntimeId // Needed for preprocessor
						def commandArgs = [
							new ActualParameter(
								actualParameterName: "shellToUse",
								value: "ec-groovy"
							),
							new ActualParameter(
								actualParameterName: "commandToRun",
								value: """\\
									import com.electriccloud.client.groovy.ElectricFlow
									import com.electriccloud.client.groovy.models.ActualParameter
									ElectricFlow ef = new ElectricFlow()
									def flowRuntimeId = ef.runPipeline(
										projectName: "${step.project}",
										pipelineName: "${step.pipeline}",
										//actualParameters : args
									).flowRuntime.flowRuntimeId
									def pipelineProject = ef.getProperty(propertyName: "/myPipelineRuntime/projectName", flowRuntimeId:flowRuntimeId).property.value
									def pipelineName = ef.getProperty(propertyName: "/myPipelineRuntime/pipelineName", flowRuntimeId:flowRuntimeId).property.value
									def pipelineId = ef.getPipeline(projectName:pipelineProject, pipelineName: pipelineName).pipeline.pipelineId
									// TODO: Why are pipelineId and flowRuntimeId null? Seem to be using outside context
									def linkValue = "<http><a href=\\\\"https://$[/server/settings/ipAddress]/flow/#pipeline-run/" +
										pipelineId + "/" +
										flowRuntimeId +
										"\\\\">Pipeline Run</a></http>"
									ef.setProperty(
										propertyName: "summary",
										value: linkValue
									)									
									""".stripIndent()
							)								
						]
						def jobStepId = ef.createJobStep(
							jobStepName : step.name,
							jobId:jobId,
							parentPath: "/jobSteps/${scriptParentJobStep.jobStep.jobStepId}",
							resourceName: resource, 
							workspaceName: '$[/myWorkspace/workspaceName]',
							subproject : ef.getPlugin(pluginName:"EC-Core").plugin.projectName,
							subprocedure : "runCommand",
							actualParameters : commandArgs
						).jobStep.jobStepId				
					}
				}
			}
			'''.stripIndent()
	}
}