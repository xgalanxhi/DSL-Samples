/*

CloudBees CD DSL: Poll ServiceNow using ec-groovy

This procedure can be used for pipeline gates.

TODO


*/

project "ServiceNow",{
	procedure "Poll for target state",{
		formalParameter "TargetState", required: true, description: "Change request desired end state"
		formalParameter "Configuration", required: true, description: "EC-ServiceNow configuration"
		formalParameter "PollingInterval", required: false, defaultValue: "60", description: "Polling Interval in seconds"
		formalParameter "RecordID", required: true, description: "Record to be queried"
		
		step 'Get State', shell: 'ec-groovy', command: '''\
			import groovy.json.JsonSlurper
			import com.electriccloud.client.groovy.ElectricFlow
			import com.electriccloud.client.groovy.models.ActualParameter
			ElectricFlow ef = new ElectricFlow()
			
			def PollingInterval = $[PollingInterval]
			
			GetApprovalStatus = {
				def params = [
					new ActualParameter('config_name', '$[Configuration]'),
					new ActualParameter('property_sheet', '/myJob'),
					new ActualParameter('record_id', '$[RecordID]'),

				]	  
				def RunResponse = ef.runProcedure procedureName: 'GetRecord', projectName: '/plugins/EC-ServiceNow/project', 		actualParameters: params

				def JobId = RunResponse.jobId
				ef.setProperty propertyName: "/myJob/report-urls/Get Status Job", value: "link/jobDetails/jobs/${JobId}"
				
				
				// Not available in ec-groovy
				//ef.waitForJob jobId: JobId
				sleep 5000 // 5 seconds
				
				def SN_ResponseJson = ef.getProperty(propertyName: "/myJob/ResponseContent", jobId: JobId).property.value
				def Slurper = new JsonSlurper()
				def ApprovalStatus = Slurper.parseText(SN_ResponseJson)[0].approval
				def SysId = Slurper.parseText(SN_ResponseJson)[0].sys_id
				ef.setProperty propertyName: "/myJob/report-urls/ServiceNow Record: $[RecordID]", value: "\$[/plugins/EC-ServiceNow/project/ServiceNow_cfgs/\$[Configuration]/host]/nav_to.do?uri=change_request.do?sys_id=${SysId}"
				println "Approval status: $ApprovalStatus"
				return ApprovalStatus
			}
			
			while (GetApprovalStatus() != "approved") {
				sleep PollingInterval * 1000
			}
			
			
		'''.stripIndent()
	}
}