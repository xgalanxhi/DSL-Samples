/*

CloudBees CD DSL: Use of agent environment variables

This feature was introduced in preview version 2020.12
https://docs.cloudbees.com/docs/cloudbees-cd/preview/configure/agent-env-vars

*/

property "/resources/local/ec_environment_variables/DSLSAMPLE_VARIABLE", value: "Test value 123"

project "DSL-Samples", {
	procedure "Agent environment variables", {
		step "Echo variable value",
			description: 'Echo the varialble and set the job step summary to that value as well',
			resourceName: 'local',
			command: '''\
				echo $DSLSAMPLE_VARIABLE
				ectool setProperty summary "$DSLSAMPLE_VARIABLE"
			'''.stripIndent()
	}
}