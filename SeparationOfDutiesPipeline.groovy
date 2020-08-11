/*

CloudBees DSL: Separation of duties in pipeline execution

This pipeline illustrates how to use access controls (ACL enteries) to enforce separation of duties for software release. Stage A can only be run by user A and Stage B can only be run by user B. This is analogous to developers being able to start a release pipeline and execute a development stage, but only testers can run the testing stage. A manual aproval by user B enables ownership of the pipeline run to be assumed by user B so that Stage B can be run.

Instruction
1. Update session password below
2. Apply this DSL
3. Start pipeline as user A
4. Approve as user B

*/

def SessionPassword = "changeme"

user "A", password: "changeme", sessionPassword: SessionPassword
user "B", password: "changeme", sessionPassword: SessionPassword

project "Separation of duties",{
	acl {
		inheriting = '1'
		aclEntry 'group', principalName: 'Everyone', {
			readPrivilege = 'allow'
			executePrivilege = 'allow'
		}
	}
	pipeline 'Staged access', {
		stage 'A', {
			acl {
				inheriting = '0' // break inheritance
				aclEntry 'group', principalName: 'Everyone', {
					readPrivilege = 'allow'
				}				
				aclEntry 'user', principalName: 'A', {
					executePrivilege = 'allow'
				}
				
			}
			gate 'POST', {
				task 'B', {
					gateType = 'POST'
					notificationEnabled = '1'
					notificationTemplate = 'ec_default_gate_task_notification_template'
					taskType = 'APPROVAL'
					useApproverAcl = '1'
					approver = [
						'B',
					]
				}
			}

			task 'echo', {
				actualParameter = [
					'commandToRun': 'echo test',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}

		stage 'B', {
			acl {
				inheriting = '0'
				aclEntry 'group', principalName: 'Everyone', {
					readPrivilege = 'allow'
				}
				aclEntry 'user', principalName: 'B', {
					executePrivilege = 'allow'
				}

			}
			task 'echo', {
				actualParameter = [
					'commandToRun': 'echo',
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}
	}
}