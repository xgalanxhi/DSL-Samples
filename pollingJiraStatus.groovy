project 'Ticket Status Gate', {
  tracked = '1'

  procedure 'Poll Jira for Target Status', {

    formalParameter 'Configuration', {
      description = 'EC-JIRA configuration'
      orderIndex = '1'
      required = '1'
      type = 'entry'
    }

    formalParameter 'JQL', {
      description = 'JQL'
      orderIndex = '2'
      required = '1'
      type = 'textarea'
    }

    formalParameter 'PollingInterval', defaultValue: '60', {
      description = 'Polling Interval in seconds'
      orderIndex = '3'
      type = 'entry'
    }

    formalParameter 'TargetState', {
      description = 'Change request desired end state'
      orderIndex = '4'
      required = '1'
      type = 'textarea'
    }

    step 'Get State', {
      command = '''import groovy.json.JsonSlurper
import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.ActualParameter
ElectricFlow ef = new ElectricFlow()

def PollingInterval = $[PollingInterval]
println "JQL = $[JQL]"

GetApprovalStatus = {
	def params = [
		new ActualParameter(\'config\', \'$[Configuration]\'),
		new ActualParameter(\'jql\', \'$[JQL]\')
	]	  
	def RunResponse = ef.runProcedure procedureName: \'GetIssues\', projectName: \'/plugins/EC-JIRA/project\', 		actualParameters: params
	def JobId = RunResponse.jobId
    
	ef.setProperty propertyName: "/myJob/report-urls/Get Status Job", value: "link/jobDetails/jobs/${JobId}"	

    // Wait for job
	def JobStatus
	while ((JobStatus = (String) ef.getJobStatus(jobId: JobId).status) != "completed") {
		println "Job status: " + JobStatus
		ef.setProperty propertyName: "/myJobStep/summary", value: """<html><a href="${JobId}"> Polling JIRA</a></html>"""
		sleep 5000 // 5 seconds
	}
	if ((JobOutcome = (String) ef.getJobStatus(jobId: JobId).outcome) == "success") {
		def issueKey = ef.getProperty(propertyName: "/myJob/getIssuesResult/issueKeys", jobId: JobId).property.value
        println "issueKey = " + issueKey
        
        def ApprovalStatus = ef.getProperty(propertyName: "/myJob/getIssuesResult/issues/${issueKey}/status", jobId: JobId).property.value
        
		println "Jira status: $ApprovalStatus"
		return ApprovalStatus
	} else {
		// JIRA plugin job failed
		println "The EC-JIRA job has failed. Click the Get Status Job link to debug."
		ef.setProperty propertyName: "/myJobStep/summary", value: """<html><a href="${JobId}"> Plugin Job Failed</a></html>"""
		System.exit(1)
		return "PluginError"
	}
}

while (GetApprovalStatus() != \'$[TargetState]\') {
	sleep PollingInterval * 1000
}
ef.setProperty propertyName: "/myJobStep/summary", value: """Target state reached"""

'''
      shell = 'ec-groovy'
      subprocedure = ''
      subproject = ''
    }
  }
}
