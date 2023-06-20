project 'Credential Examples', {

	credential 'user1', userName: 'user1', password: "user1password", passwordRecoveryAllowed = true
	credential 'user2', userName: 'user2', password: "user2password", passwordRecoveryAllowed = true

	procedure 'Get Credentials with javascript', {
	
		formalParameter "Credential", required: true, defaultValue: "user1"
	
		step 'Get Credentials', command : '''\
			echo USER: $[/javascript 
			var creds = {
			credentialName : "user1",
				jobStepId: myJobStep.jobStepId
			}
			api.getFullCredential(creds).credential.userName
			]

			echo PASS: $[/javascript 
			var creds = {
			credentialName : "user1",
				jobStepId: myJobStep.jobStepId
			}
			api.getFullCredential(creds).credential.password
			]
			'''.stripIndent(),{
				attachCredential(
					credentialName: 'user1',
				)				
			} // step
			
		step 'Get Credentials with ectool', command : '''\
			ectool getFullCredential user1 --value userName
			ectool getFullCredential user1 --value password
			'''.stripIndent(),{
				attachCredential(
					credentialName: 'user1',
				)				
			} // step			
			
		step "Dynamic credentials with createJobStep", shell: 'ec-perl', command: '''\
			use strict;
			use ElectricCommander;
			$| = 1;
			my $ec = new ElectricCommander();

			$ec->createJobStep({
				command  =>  "ectool getFullCredential $[Credential] --value userName;ectool getFullCredential $[Credential] --value password",
			});
			
		'''.stripIndent()

	} // procedure
	

}
