/*

CloudBees CD (Flow) DSL: Use of credentials from a different project

In this example, credentials are stored in one project and used in a pipeline task in another project

*/


def CredentialName = "MyCreds"

def CredentialProject = "Credential Project"

project CredentialProject,{
	credential CredentialName, userName: "MyUser", password: "My password"
}

project "Use Credential Project",{
	procedure 'Command line credentials', {
		step 'Use credentials in a shell', {
			description = 'ec-groovy wrapped command line that uses credential values'
			shell = 'ec-groovy'
			command = """\
				import com.electriccloud.client.groovy.ElectricFlow
				ElectricFlow ef = new ElectricFlow()
				def userName=ef.getFullCredential(credentialName: "/projects/${CredentialProject}/credentials/$CredentialName").credential.userName
				def password=ef.getFullCredential(credentialName: "/projects/${CredentialProject}/credentials/$CredentialName").credential.password
				/*
					This example uses 'echo' to purposefully show the credentials as proof that this mechanism works. When using this approach, make sure not to echo the secret part of the credential.
				*/
				println "echo Username: \$userName, Password: \$password".execute().text
			""".stripIndent()
			attachCredential {
				credentialName = "/projects/${CredentialProject}/credentials/$CredentialName"
			}
		}
	}
	pipeline "Use Credentials from different project",{
		stage "Stage 1",{
			task 'Use credential', {
				subprocedure = 'Command line credentials'
				subproject = projectName
				taskType = 'PROCEDURE'
			}			
		}
	}
}