/*

CloudBees SDA ARO DSL: Use of credentials in a pipeline task

*/

project "DSL-Samples", {
	credential 'credname', userName: 'usern', password: "passxxx", credentialType: 'LOCAL'
	procedure "The procedure",{
		step "The step", command: "echo", credentialName: 'credname'
	}
	pipeline "The pipeline", {
		stage "The stage",{
			task "The task",{
				taskType = 'COMMAND'
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				actualParameter = [
					commandToRun: '''\
						CRED_USER=$(ectool getFullCredential credname --value userName)
						CRED_PW=$(ectool getFullCredential credname --value password)
						echo "User: ${CRED_USER}, password: ${CRED_PW}"
					'''.stripIndent(),
				]
				attachCredential credentialName: "credname"
			}
			task "Run the procedure",{
				subproject = projectName
				taskType = 'PROCEDURE'
				subprocedure = 'The procedure'
			}
		}
	}
}