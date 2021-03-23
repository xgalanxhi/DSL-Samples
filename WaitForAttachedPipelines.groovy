/*

Wait for attached pipelines to complete before advancing release

Instructions
- Apply this DSL by cutting and pasting to the DSLIDE or by running
		ectool evalDsl --dslFile WaitForAttachedPipelines.groovy
- Navigate the the running release, note that QA is waiting
- Navigate to the Release Portfolio list
- Open each of the attached pipeline runs and do the approval so the pipelines continue
- The release should continue now

What this DSL does
- Creates two pipelines
- Creates a Release
- Runs the pipelines
- Attaches them to the release
- Starts the release and runs it
- The release is configured so that the QA stage will only start once the attached pipelines have completed

*/

project "DSL-Samples",{
	release "The release",{
		pipeline "The release pipeline",{
			stage "Dev",{
				task 'Promote code', {
					taskType = 'COMMAND'
					actualParameter = [
						commandToRun: 'echo promoting',
					]
				}
			}
			stage "QA",{
				precondition = '''\
					$[/javascript
						var attachedRuns =	api.getAttachedPipelineRuns(
												{
													projectName: myRelease.projectName,
													releaseName: myRelease.releaseName
												}
											).attachedPipelineRunDetail
						var allCompleted=true
						if (typeof(attachedRuns)!=='undefined')
							for (i=0; i < attachedRuns.length; i++) {
								var isCompleted =	api.getProperty(
													{
														flowRuntimeId: attachedRuns[i].flowRuntimeId,
														propertyName: "/myPipelineRuntime/completed"
													}
												).property.value
								
								if (isCompleted=="false") allCompleted=false
							}
						setProperty("/myPipelineRuntime/allCompleted",allCompleted)
						allCompleted?"true":"false"
					]
				'''.stripIndent()
				task 'Run tests', {
					precondition = '' // workaround for rogue inherritance (to be fixed post v10.1)
					taskType = 'COMMAND'
					actualParameter = [
						commandToRun: 'echo testing',
					]
				}
			}
			stage "UAT",{
				task 'User testing', {
					taskType = 'COMMAND'
					actualParameter = [
						commandToRun: 'echo user testing',
					]
				}
			}
			stage "PROD",{
				task 'Production deployment', {
					taskType = 'COMMAND'
					actualParameter = [
						commandToRun: 'echo deploying to production',
					]
				}
			}
		}
	}
	pipeline "Component A",{
		stage "Merge to Main",{
			gate 'POST', {
				task 'Approve for release', {
					taskType = 'APPROVAL'
					approver = [
						'Everyone',
					]
				}			
			}
		}
	}
	pipeline "Component B",{
		stage "Merge to Main",{
			gate 'POST', {
				task 'Approve for release', {
					taskType = 'APPROVAL'
					approver = [
						'Everyone',
					]
				}			
			}
		}
	}
}

def A=runPipeline(projectName: "DSL-Samples", pipelineName: "Component A").flowRuntimeId
	B=runPipeline(projectName: "DSL-Samples", pipelineName: "Component B").flowRuntimeId
runPipeline projectName: "DSL-Samples", pipelineName: "The release pipeline", releaseName: "The release"
attachPipelineRun projectName: "DSL-Samples", releaseName: "The release", flowRuntimeId: A
attachPipelineRun projectName: "DSL-Samples", releaseName: "The release", flowRuntimeId: B
startRelease projectName: "DSL-Samples", releaseName: "The release"
runPipeline projectName: "DSL-Samples", pipelineName: "The release pipeline", releaseName: "The release"

