project "Pipeline Output Parameters", {
	procedure "Set output parameter", {
		formalOutputParameter "Out"
		step "Set Out output parameter", 
			command : "ectool setOutputParameter Out 123"
	}
	pipeline "Use output parameter",{
		stage "Stage 1",{
			task 'Set output parameter', {
				subprocedure = 'Set output parameter'
				subproject = projectName
				taskType = 'PROCEDURE'
			}
			task "Use output parameter",{
				actualParameter = [
					'commandToRun': 'echo \$[/myStageRuntime/tasks/Set output parameter/job/outputParameters/Out]',
				]
				taskType = 'COMMAND'			
			}
		}
	}
}