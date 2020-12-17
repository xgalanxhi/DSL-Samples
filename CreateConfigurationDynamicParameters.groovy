def Credentials = "UserCredentials"

project "Demo Onboarding",{
	procedure "Create EC-Jenkins configuration",{
		formalParameter "JenkinsConfig", description: "Name of configuration to be created", required: true
		formalParameter "ServerUrl", description: "For example https://myhost.com/controllerA:8080", required: true
		formalParameter Credentials, type: 'credential', required: true
		
		step "Create Configuration",{
			command = """\
				_USERNAME=\$(ectool getFullCredential ${Credentials} --value userName)
				_PASSWORD=\$(ectool getFullCredential ${Credentials} --value password)

				printf "\${_PASSWORD}\\n" |ectool runProcedure /plugins/EC-Jenkins/project \\
					--procedureName CreateConfiguration \\
					--actualParameter \\
						config="\$[JenkinsConfig]" \\
						server="\$[ServerUrl]" \\
						test_connection="false" \\
						credential="${Credentials}" \\
					--credential "${Credentials}"="\${_USERNAME}"
			
				""".stripIndent()
			attachParameter formalParameterName : Credentials
		}
	}
}