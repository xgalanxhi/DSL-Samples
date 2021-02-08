/*

CloudBees CD DSL: REST API access using Session ID

Currently the CloudBees CD has API bindings for Perl and Groovy. API calls can be made to CloudBees CD from
procedure steps using REST. Authentication for the call can be made using the job step session ID. The
procedure example below illustrate how to use the sesssion ID to make calls from curl and Python.

*/


project "DSL-Samples", {
	procedure "REST calls with sessionid", {
		step "curl",
			command : 'curl -k -H "sessionid: $COMMANDER_SESSIONID" https://flow/rest/v1.0/projects'
		step "python", shell: "python '{0}'", command: '''\
			import requests

			resp = requests.get('https://flow/rest/v1.0/projects', headers={'sessionid':os.environ.get('COMMANDER_SESSIONID')}, verify=False)
			if resp.status_code != 200:
				# Error condition
				print(resp.status_code)
			else:
				print(resp.json())
		'''.stripIndent()
	}
}