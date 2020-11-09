import groovy.json.JsonOutput

def CDPipeline = "Jenkins initiated"
def CDProject = "Jenkins CD"
def CDConfig = "CD"
def CDFirstStage = "Stage 1"

def Params = [
	pipeline: [
		pipelineName: CDPipeline,
		parameters: [
			[
				parameterName:"BuildId",
				parameterValue:"${BUILD_NUMBER}"
			],
			[
				parameterName:"ComponentVersion",
				parameterValue:"5.1"
			]
		]
	]
]

pipeline{
	agent any
	stages{
		stage(CDFirstStage){
			steps{
				cloudBeesFlowRunPipeline addParam: JsonOutput.toJson(Params),
					configuration: CDConfig,
					pipelineName: CDPipeline,
					projectName: CDProject
			}
		}
	}
}
