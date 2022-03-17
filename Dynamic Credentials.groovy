/*

Cloudbees Flow DSL: Use of dynamic credentials parameters

Dynamic credentials are those that are entered as parameters at runtime and only last the duration of a job or pipeline
 run. These can be used as an alternative to static credentials which are saved to a project. The ideas is that a user
 can be prompted for credentials when they are needed for the execution to pipeline tasks.

*/

def Credentials = "creds"
def ProcedureName = 'Dynamic Credentials'

project "Credential Examples",{
	procedure ProcedureName, {
		formalParameter Credentials, type: 'credential'
		step 'Use Credentials',{
			command = """\
				user=\$(ectool getFullCredential ${Credentials} --value userName)
				pass=\$(ectool getFullCredential ${Credentials} --value password)
				
				# Sample command below to show successful passing of the credentials. Make sure
				# not to echo out the hidden \${user} and \${pass} values in you work.
				echo "User: \${user}, Password: \${pass}"
			""".stripIndent()
			attachParameter formalParameterName : Credentials
		}
  	}
	pipeline 'Use Dynamic Credentials', {
		formalParameter Credentials, type: 'credential', required: true
		stage 'QA', {
			task 'Run credential procedure', {
				actualParameter = [
						creds: Credentials,
				]
				subprocedure = ProcedureName
				subproject = projectName
				taskType = 'PROCEDURE'
				attachParameter formalParameterName : Credentials
			}
		}
	}
}
