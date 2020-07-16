/*

CloudBees Flow DSL: Using credentials on the command line in a secure way

When it is necessary to supply credentials in a Command block in Flow, there are several options:
1. Include the credentials values in the command. This exposes the credentials in the step or task definition and the job log file.
2. Use $[/javascript api.getFullCredential(...).password] to retrieve the credentials at runtime. In this case, the credentials will not be exposed in the step definition, but they will be exposed in the job log file.
3. Wrap the command line call in an ec-perl or ec-groovy script. This will prevent the credentials from being exposed in both the definition and the job log file.
5. Use environment variables and $(ectool getFullCredential ...) to avoid exposing the credentials in log files or the command definition.

This example illustrates how wrap a command line call in ec-groovy and also how to run a shell command. It creates a secure credential and a procedure with a step that uses that credential.

Note that credentials stored in a different project can be used, but must be fully qualified:

/projects/My Cred Project/credentials/My Cred
/projects/$[/plugins/EC-Jenkins]/credential/My Jenkins Creds


*/

def CredentialName = "myuser"

project "Credentials",{
	credential CredentialName, userName: "myuser", password: "mypass"
	procedure 'Command line credentials', {
		step 'Use credentials in a ec-groovy', {
			description = 'ec-groovy wrapped command line that uses credential values'
			shell = 'ec-groovy'
			command = """\
				import com.electriccloud.client.groovy.ElectricFlow
				ElectricFlow ef = new ElectricFlow()
				def userName=ef.getFullCredential(credentialName: "${CredentialName}").credential.userName
				def password=ef.getFullCredential(credentialName: "${CredentialName}").credential.password

				/*
					This example uses 'echo' to purposefully show the credentials as proof that this mechanism works. When using this approach, make sure not to echo the secret part of the credential.
				*/
				println "echo Username: \$userName, Password: \$password".execute().text
			""".stripIndent()
			attachCredential {
				credentialName = "/projects/$projectName/credentials/$CredentialName"
			}
		}
		step 'Use credentals in a shell',{
			command = """\
				USER=\$(ectool getFullCredential "${CredentialName}" --value userName)
				PASS=\$(ectool getFullCredential "${CredentialName}" --value password)
				echo \$USER
				echo \$PASS > /dev/null
			""".stripIndent()
			attachCredential {
				credentialName = "/projects/$projectName/credentials/$CredentialName"
			}
		}
	}
}
