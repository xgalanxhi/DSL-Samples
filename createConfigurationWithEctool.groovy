/*

CloudBees CD DSL

Creating plugin configurations from the command line

CloudBees CD plugins use configurations to manage credentials and other information needed to use the third party integration, such as a URL. These configurations are created using a procedure, usually named "CreateConfiguration" in the plugin project. Here is an example command line that can be used to create a Git configuration:

printf 'apassword\n' | \
	ectool runProcedure /plugins/ECSCM-git/project \
	--procedureName CreateConfiguration \
	--actualParameter \
		config=myectoolcreds \
		credentialType=password \
		credential=myectoolcreds \
	--credential myectoolcreds=auser
	
Where the following values should be edited:
- myectoolcreds (three instances)
- auser
- apassword

Note that the password is piped in using the printf command. This can be replaced with a call to a secrets too such as Hashicorp Vault. Done in this way, the password is not exposed.

The example procedure below uses dynamic credentials to create at ECSCM-git configuration.

*/

def Credentials = "creds"

project "DSL-Samples",{
	procedure 'Create ECSCM-git Configuration', {
		formalParameter 'Configuration', required: true
		formalParameter 'creds', type: 'credential'
		step 'Create Configuration',{
			command = '''\
				CRED_USERNAME=$(ectool getFullCredential creds --value userName)
				CRED_PASSWORD=$(ectool getFullCredential creds --value password)
				CRED_RESPONSE=$(printf "${CRED_PASSWORD}\n" |ectool runProcedure /plugins/ECSCM-git/project \
				--procedureName CreateConfiguration \
				--actualParameter \
					config="$[Configuration]" \
					credentialType=password \
					credential="$[Configuration]" \
				--credential "$[Configuration]"="${CRED_USERNAME}" \
				)
				CRED_JOBID=$(echo $CRED_RESPONSE | cut -d" " -f3) # Extract JobId from user and password: prompt
				echo Cred job: $CRED_JOBID
				ectool setProperty "/myJob/report-urls[CreateConfiguration Job]" --value "link/jobDetails/jobs/${CRED_JOBID}"
			'''.stripIndent()
			attachParameter formalParameterName : "/projects/${projectName}/procedures/${procedureName}/formalParameters/${Credentials}"
		}
	property "ec_customEditorData/parameters/creds", formType: "standard"
  }
}
