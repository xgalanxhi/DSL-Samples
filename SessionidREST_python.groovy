/*

CloudBees CD DSL: REST API access using Session ID

Currently the CloudBees CD has API bindings for Perl and Groovy. API calls can be made to CloudBees CD from
procedure steps using REST. Authentication for the calls can be made using the job step session ID. The
procedure example below illustrates how to use the sesssion ID to make calls from curl and Python.

Instructions
1. Replace "flow" in the URLs with your CloudBees CD hostname
2. Apply this DSL through the DSL IDE or from the command line:
	ectool evalDsl --dslFile SessionidREST_python.groovy

*/


project "DSL-Samples", {
	procedure "REST calls with sessionid", {
		step "curl",
			command : 'curl -k -H "sessionid: $COMMANDER_SESSIONID" https://flow/rest/v1.0/projects'
		step "python", shell: "python '{0}'", command: '''\
			import requests
			import os
			
			def getProjects():
				resp = requests.get('https://flow/rest/v1.0/projects', headers={'sessionid':os.environ.get('COMMANDER_SESSIONID')}, verify=False)
				if resp.status_code != 200:
					# Error condition
					return resp.status_code
				else:
					return resp.json()
					
			def getProject(projectName):
				resp = requests.get('https://flow/rest/v1.0/projects/' + projectName, headers={'sessionid':os.environ.get('COMMANDER_SESSIONID')}, verify=False, )
				if resp.status_code != 200:
					# Error condition
					return resp.status_code
				else:
					return resp.json()
			
			print getProjects()
			print getProject("Default")
			
		'''.stripIndent()
		}
}
