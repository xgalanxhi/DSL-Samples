/*

Cloudbees Flow DSL: Use of dynamic credentials

Dynamic credentials are those that are entered at runtime and only last the duration of a job

*/

def Credentials = "creds"

project "Credential Examples",{
	procedure 'Dynamic Credentials', {
		formalParameter Credentials, type: 'credential'
		step 'Use Credentials',{
			command = """\
				user=\$(ectool getFullCredential ${Credentials} --value userName)
				pass=\$(ectool getFullCredential ${Credentials} --value password)
				
				# Sample command below to show successful passing of the credentials. Make sure
				# not to echo out the hidden ${user} and ${pass} values in you work.
				echo "User: \${user}, Password: \${pass}"
			""".stripIndent()
			attachParameter formalParameterName : Credentials
		}
	property "ec_customEditorData/parameters/${Credentials}", formType: "standard"
  }
}
