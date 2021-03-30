/*

CloudBees SDA CD/RO DSL: Create and Run Release and Subpipelines

*/

project "DSL-Samples",{
	["Sub1","Sub2"].each { SubPipeline ->
		pipeline SubPipeline,{
			stage "Stage 1",{
				task "Set release output parameter",{
					actualParameter = [
						commandToRun: (String) "ectool setOutputParameter ${SubPipeline} \"${SubPipeline} output\" --flowRuntimeId \$[/myTriggeringPipelineRuntime/flowRuntimeId]",
					]
					subpluginKey = 'EC-Core'
					subprocedure = 'RunCommand'
					taskType = 'COMMAND'
				}
			}
		}
		["Rel1","Rel2"].each { Rel ->
			release Rel,{
				pipeline Rel,{
					formalOutputParameter SubPipeline
					stage "Dev",{
						task SubPipeline,{
							taskType = "PIPELINE"
							subpipelineProject = projectName
							subpipeline = SubPipeline
						}
					}
				}
			}
		}		
	}
}

["Rel1","Rel2"].each { Rel ->
	startRelease projectName: "DSL-Samples", releaseName: Rel
}
