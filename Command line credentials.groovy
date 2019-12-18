project "Credentials",{
	credential "myuser", userName: "myuser", password: "mypass"
	procedure 'Command line credentials', {
		description = 'How to use credentials from the command line in a secure way'

		step 'Use credentials in a shell', {
			description = ''
			command = '''\
				import com.electriccloud.client.groovy.ElectricFlow
				ElectricFlow ef = new ElectricFlow()
				def userName=ef.getFullCredential(credentialName: "myuser").credential.userName
				def password=ef.getFullCredential(credentialName: "myuser").credential.password

				println "echo Username: $userName, Password: $password".execute().text
			'''.stripIndent()
			shell = 'ec-groovy'
			attachCredential {
				credentialName = "/projects/$projectName/credentials/myuser"
			}
		}
	}
}
