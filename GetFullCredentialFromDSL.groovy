/*

CloudBees CDRO DSL: Extract credential value in a DSL step

This example illustrates how to extract a password from a credential using the
getFullCredential() API from the Groovy DSL. The API is called from a procedure
step which has a credential parameter attached. When the procedure is run, the
user is prompted to enter credential values. The step stores the password to a
property--of course, in a real-world use case the password would not be exposed
in this way, but used to create another credential or to issue an API with
authentication.

*/

project "DSL-Samples",{
	procedure 'Get full credentials with DSL', {
		formalParameter 'cred', required: true, type: 'credential'
		step 'Get password', {
			shell = 'ectool evalDsl --dslFile'
			command = '''\
				property "/myJob/cred",
					value: getFullCredential(
						credentialName:"cred",
						jobStepId: "$[/myJobStep/jobStepId]"
					).password
			'''.stripIndent()
			attachParameter {
				formalParameterName = "/projects/${projectName}/procedures/${procedureName}/formalParameters/cred"
			}
		}
	}
}