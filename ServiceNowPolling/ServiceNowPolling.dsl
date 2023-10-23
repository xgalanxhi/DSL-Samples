/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

CloudBees CD DSL: Poll ServiceNow using ec-groovy

This procedure can be used for pipeline gates.

TODO

*/

def currentProject = 'dslsamples'


project currentProject, {
procedure 'Poll SNOW for target state', {

  formalParameter 'Configuration', defaultValue: '', {
    description = 'EC-ServiceNow configuration'
    orderIndex = '1'
    propertyReference = 'EC-ServiceNow'
    required = '1'
    type = 'pluginConfiguration'
  }

  formalParameter 'PollingInterval', defaultValue: '60', {
    description = 'Polling Interval in seconds'
    orderIndex = '2'
    type = 'integer'
  }

  formalParameter 'RecordID', {
    description = 'Record to be queried'
    required = '1'
    type = 'textarea'
  }

  formalParameter 'TargetState', {
    description = 'Change request desired end state'
    required = '1'
    type = 'textarea'
  }

  step 'Get State', shell: 'ec-groovy', command: '''\
import groovy.json.JsonSlurper
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.ActualParameter
ElectricFlow ef = new ElectricFlow()

def PollingInterval = $[PollingInterval]
def RecordID = ef.getProperty(propertyName: \'/myJob/RecordID\').property.value
println "Record ID = ${RecordID}"
def ServiceNowHost = \'\'

def projectRegex = \'/projects/(?<project>.*)/pluginConfigurations/(?<config>.*)\'
def contentMatcher = ( "$[Configuration]" =~ projectRegex )
contentMatcher.matches()
project_name = contentMatcher.group("project")
config_name = contentMatcher.group("config")


println "#################################"
println "$[Configuration]"
println "Project Name = " + project_name
println "Configuration = " + config_name

def result = ef.getPluginConfiguration(
				projectName: project_name,
				pluginConfigurationName: config_name ).fields.each { 
					if (it.parameterName=="host") ServiceNowHost=it.parameterValue 
				}

GetApprovalStatus = {
	def params = [
		new ActualParameter(\'config_name\', \'$[Configuration]\'),
		new ActualParameter(\'property_sheet\', \'/myJob\'),
		new ActualParameter(\'record_id\', RecordID)

	]
	def RunResponse = ef.runProcedure procedureName: \'GetRecord\', projectName: \'/plugins/EC-ServiceNow/project\', 		actualParameters: params

	def JobId = RunResponse.jobId
	ef.setProperty propertyName: "/myJob/report-urls/Get Status Job", value: "link/jobDetails/jobs/${JobId}"

	// Wait for job
	def JobStatus
	while ((JobStatus = (String) ef.getJobStatus(jobId: JobId).status) != "completed") {
		println "Job status: " + JobStatus
		ef.setProperty propertyName: "/myJobStep/summary", value: """<html><a href="${JobId}"> Polling ServiceNow</a></html>"""
		sleep 5000 // 5 seconds
	}
	if ((JobOutcome = (String) ef.getJobStatus(jobId: JobId).outcome) == "success") {
		def SN_ResponseJson = ef.getProperty(propertyName: "/myJob/ResponseContent", jobId: JobId).property.value
		def Slurper = new JsonSlurper()
		def ApprovalStatus = Slurper.parseText(SN_ResponseJson)[0].approval
		def SysId = Slurper.parseText(SN_ResponseJson)[0].sys_id
		ef.setProperty propertyName: "/myJob/report-urls/ServiceNow Record", value: "${ServiceNowHost}/nav_to.do?uri=change_request.do?sys_id=${SysId}"
		println "Approval status: $ApprovalStatus"
		return ApprovalStatus
	} else {
		// ServiceNow plugin job failed
		println "The EC-ServiceNow job has failed. Click the Get Status Job link to debug."
		ef.setProperty propertyName: "/myJobStep/summary", value: """<html><a href="${JobId}"> Plugin Job Failed</a></html>"""
		System.exit(1)
		return "PluginError"
	}

}

while (GetApprovalStatus() != \'$[TargetState]\') {
	sleep PollingInterval * 1000
}
ef.setProperty propertyName: "/myJobStep/summary", value: """Target state reached"""

	'''.stripIndent()
  }
}