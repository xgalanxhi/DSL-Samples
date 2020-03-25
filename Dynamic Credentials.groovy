/*

Cloudbees Flow DSL: Use of dynamic credentials

Dynamic credentials are those that are entered at runtime and only last the duration of a job

*/

def Credentials = "creds"

project "Default",{
	procedure 'Dynamic Credentials', {
		formalParameter 'creds', type: 'credential'
		step 'Get Credentials',{
			command = '''\	
				ectool getFullCredential creds --value userName
				ectool getFullCredential creds --value password
			'''.stripIndent()
			attachParameter formalParameterName : "/projects/${projectName}/procedures/${procedureName}/formalParameters/${Credentials}"
		}
	property "ec_customEditorData/parameters/creds", formType: "standard"
  }
}
