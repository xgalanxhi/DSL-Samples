//dsl

/*

CloudBees CD DSL for Unplug: Create table of pipeline metrics

ectool setProperty /server/unplug/vh --valueFile UnplugPipelineHealth.groovy

https://<FlowServerURL>/commander/pages/unplug/un_runh?ProjectName=Pipeline%20Health

*/

def ProjectName = (args?.ProjectName)?:""

def Pipelines = getPipelineRuntimes(projectName: ProjectName)
def writer = new StringWriter()  // html is written here by markup builder
def markup = new groovy.xml.MarkupBuilder(writer)  // the builder
markup.html {
	h1 ("Running Pipeline Health Report")
	h2 ("Project name: ${ProjectName}")
	table (class:"data",cellspacing:"0") {
		tr (class: "headerRow", style: "font-size:16px;") {
			th("Pipeline Name")
			th("Current Stage")
			th("Elapse Time")
			th("Code Coverage (Target 94%)")
			th("Integraged Test Results (Target 90%)")
		}
		Pipelines.each { Pipeline ->
			if (!Pipeline.completed) {
				def CodeCoverage = getProperty(propertyName: "CodeCoverage",
					flowRuntimeId: Pipeline.flowRuntimeId).value.toDouble()
				def PassingIntegratedTests = getProperty(propertyName: "PassingIntegratedTests",
					flowRuntimeId: Pipeline.flowRuntimeId).value.toDouble()
				def FailingIntegratedTests = getProperty(propertyName: "FailingIntegratedTests",
					flowRuntimeId: Pipeline.flowRuntimeId).value.toDouble()
				def CodeCoverageColor = (CodeCoverage>94)?"green":"red"
				def IntegragedTestsColor = (
						(PassingIntegratedTests / (PassingIntegratedTests + FailingIntegratedTests))>0.9
					)?"green":"red"
				def ElapsedTime = Calendar.instance
					ElapsedTime.clear()
					ElapsedTime.set(Calendar.SECOND, (Pipeline.elapsedTime/1000).toInteger())
					
				tr {
						td () {
							a(
								Pipeline.flowRuntimeName,
								href: "../../../flow/#pipeline-run/${Pipeline.pipelineId}/${Pipeline.flowRuntimeId}"
							)
						}
						td (Pipeline.currentStage, align: 'center')
						td (ElapsedTime.format('HH:mm:ss'), align: 'center')
						td (
							CodeCoverage, align: 'center',
							style:"background-color:${CodeCoverageColor};color:white;"
						)
						td (
							"Passing: ${PassingIntegratedTests} / Failing: ${FailingIntegratedTests}",
							style:"background-color:${IntegragedTestsColor};color:white;"
						)
				}
			}
		}
	}
}
writer.toString()