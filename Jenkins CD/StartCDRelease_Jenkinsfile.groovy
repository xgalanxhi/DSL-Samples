/*

Start a CloudBees CD Release from Jenkins using https://plugins.jenkins.io/electricflow/

*/

import groovy.json.JsonOutput

def CDRelease = "Jenkins initiated Release"
def CDPipeline = "Release Pipeline"
def CDProject = "Jenkins CD"
def CDConfig = "CD"
def CDFirstStage = "Stage 1"

def Params = [
	release: [
	    releaseName: CDRelease,
		pipelineName: CDPipeline,
		stages: [],
		// stages: [[stageName: CDFirstStage,stageValue: true]],
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
				cloudBeesFlowTriggerRelease parameters: JsonOutput.toJson(Params),
					configuration: CDConfig,
					releaseName: CDRelease,
					projectName: CDProject,
					// startingStage: CDFirstStage,
					startingStage: ''
			}
		}
	}
}