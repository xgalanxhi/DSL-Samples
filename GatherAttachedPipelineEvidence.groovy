/*

Gather attached pipeline evidence

Instructions
- Apply this DSL by cutting and pasting to the DSLIDE or by running
		ectool evalDsl --dslFile GatherAttachedPipelineEvidence.groovy
- Navigate the to release
- Note the attached pipeline runs
- Run the release
- Note the Release Readiness stage summary

What this DSL does
- Creates two pipelines
- Creates a Release which adds audit report links to the Release Readiness stage summary for each of the attached pipeline runs
- Runs the pipelines which attach themselves to the release

TODO
- Add other evidence

*/

def Release = "Gather attached pipeline evidence"

project "DSL-Samples",{
	release Release,{
		pipeline releaseName,{
			stage "Release Readiness",{
				task 'Gather Attached Pipeline Evidence', {
					taskType = 'COMMAND'
					actualParameter = [
						shellToUse: 'ec-groovy',
						commandToRun: '''\
							import com.electriccloud.client.groovy.ElectricFlow
							ElectricFlow ef = new ElectricFlow()
							def AttachedPipes = ef.getAttachedPipelineRuns(projectName: '$[/myRelease/projectName]', releaseName: '$[/myRelease]').attachedPipelineRunDetail
							AttachedPipes.each { Pipe ->
								// [completed:1, containsDependentRuns:0, flowRuntimeId:e38102f7-adb9-11eb-9644-080027cf88d2, flowRuntimeName:Component A_4_20210505115224, releaseId:0bd056a5-adb9-11eb-b626-080027cf88d2, releaseName:Gather attached pipeline evidence, status:success, stages:[stage:[[completed:1, elapsedTime:1194, index:0, markedCompletedTime:2021-05-05T15:52:28.333Z, name:Dev, prerun:0, progressPercentage:100, stageId:0c5194f9-adb9-11eb-b626-080027cf88d2, status:success, tasksRestartable:1, waitingForManualRetry:0, waitingForPrecondition:0, waitingOnManual:0], [completed:1, elapsedTime:1789, index:1, markedCompletedTime:2021-05-05T15:52:30.441Z, name:Release, prerun:0, progressPercentage:100, stageId:0c771d7a-adb9-11eb-b626-080027cf88d2, status:success, tasksRestartable:1, waitingForManualRetry:0, waitingForPrecondition:0, waitingOnManual:0]]]]

								ef.setProperty propertyName: """\
									/myStageRuntime/ec_summary/${Pipe.flowRuntimeName}""".stripIndent(),
									value: """\
										<html><a target="_blank" href="http://\$[/server/hostName]/flow/#audit-reports/${Pipe.flowRuntimeId}">Audit Report</a></html>""".stripIndent()
					
							}
						'''.stripIndent(),
					]
				}
			}
			stage "QA"
			stage "PROD"
		}
	}
	["A","B"].each { Stage ->
		pipeline "Component ${Stage}",{
			stage "Dev",{
				task 'Create Some Evidence', {
					taskType = 'COMMAND'
					actualParameter = [
						commandToRun: (String) 'ectool setProperty "/myStageRuntime/ec_summary/Test Results" --value '
							+ sprintf("%2.1f",100 - 10*Math.random()),
					]
				}
			}
			stage "Release",{
				task 'Attach to release', {
					taskType = 'COMMAND'
					actualParameter = [
						commandToRun: (String) "ectool attachPipelineRun \"\$[/myProject]\" --releaseName \"${Release}\" --flowRuntimeId \$[/myPipelineRuntime/flowRuntimeId]"
					]
				}
			}
		}
	}
}

runPipeline projectName: "DSL-Samples", pipelineName: "Component A"
runPipeline projectName: "DSL-Samples", pipelineName: "Component B"

