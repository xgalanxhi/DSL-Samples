/*

CloudBees Continuous Delivery DSL: Pull Request life cycle management
- Tested with BitBucket cloud

Workflow stubbed out
 1. User creates pull request from feature_branch to master branch (PR-1)
 2. Repository triggers CBCD pipeline
 3. Tool kicks off Job1 in Jenkins for PR-1. Job1 will produce an artifact
 4. On successful completion of Job1. Tool notifies user who created the PR, in Slack, that Job1 is complete
 5. Tool waits for 1 approval for PR-1. When true it will merge the feature branch into the master branch
 6. Tool starts Jenkins Job1 for the master branch
 7. On successful completion of Job1, Tool gets build artifact name and notifies the user, who approved merge, that the build is complete
 8. Tool will wait for manual approval
 9. On approval. Tool starts Job2 in Jenkins for the master branch and passes the artifact name as a parameter to Job2
10. On successful completion of Job2, Tool notifies the user, who approved merge, in Slack that the deploy was successful
11. Tool will create a Jira ticket with title "Update puppet for package $packageName", where $packageName is the name of the artifact
12. Tool will wait for manual approval
13. On approval, Tool kicks off orchestration flow#2 and passes variable $packageName, where packageName is the name of the artifact.

Setup
- Install on ECSCM-2.3.1.43 (unreleased) and configure
- Apply this DSL
	ectool evalDsl --dslFile "PR to Production.groovy"
- Apply copy WebHook.groovy to ECSMM-git
	ectool setProperty "/plugins/ECSCM-Git/project/ec_endpoints/githubWebhook/POST/script" --valueFile WebHook.groovy
- Create a session ID for BitBucket
- Walk through the trigger dialog to set the session and get the webhook trigger URL
- Create a webhook in your bitbucket repo using the URL, commit, create PR, approve PR
- [Update ProcessWebHookSchedules.perl in ECSCM] - delete me
- Make sure source control configuration listed in 'def repo' below exists
- Create a session
- Create webhook from this session in BitBucket for this Flow instance

Todo
- Restart for new commits

*/

def Project = "PR to production"
def Commit = "Commit Pipeline"
def ReleaseCandidate = "Release Cadidate Pipeline"
def Release = "Mid April 2020"

def repo = [
	label: "BitBucket",
	source: "bitbucket",
	name: "gpmaxey/demo",
	config: "greg_bitbucket",
	triggers: ["push", "created", "approved"]
]

serviceAccount repo.label
project "/plugins/ECSCM/project",{
	aclEntry principalName: repo.label, principalType: "serviceAccount", executePrivilege: "allow", modifyPrivilege: "allow"
}

project Project,{

	procedure 'Audit Reports', {
		step 'Approval', {
			subprocedure = 'generateApprovalAuditReport'
			subproject = '/plugins/EC-AuditReports/project'
		}
		step 'Evidence', {
			subprocedure = 'generateEvidenceLinksAuditReport'
			subproject = '/plugins/EC-AuditReports/project'
		}
		step 'Duration', {
			subprocedure = 'generateTaskDurationAuditReport'
			subproject = '/plugins/EC-AuditReports/project'
		}
	}

	procedure "Add Approver to Summary",{
		step "Add Approver to Summary",
			command: 'ectool setProperty "/myStageRuntime/ec_summary/PR Approver" "$[/javascript myProject.PRs[JSON.parse(myPipelineRuntime.webhookData).approvedBy+\"_approver\"]]"',
			precondition: '$[/javascript myProject.PRs[JSON.parse(myPipelineRuntime.webhookData).title+\"_approver\"]]' // Wait for the PR property to be there
	}

	procedure "Attach Pipeline to Release",{
		formalParameter "Release"
		formalParameter "ReleaseProject"
		
		step "Find Initiating Pipeline", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			def Release = '$[Release]'
			def ReleaseProject = '$[ReleaseProject]'
			def CurrentPipelineId = '$[/myPipelineRuntime/flowRuntimeId]'
			
			def getAncestor
			getAncestor = { id ->
				def Parent = ef.getProperty(propertyName: "/myTriggeringPipelineRuntime/flowRuntimeId", flowRuntimeId : id,
					/*Property Exists, this pipeline was triggered by another*/ { response, data ->
						println "Pipeline $id was triggered by ${data.property.value}"
						getAncestor(data.property.value)
					},
						/*Property does not exist, we have found the first ancestor*/ { response, data ->
						println "Pipeline $id is the first ancestor"
						return id
					}					
				
				)
			}
			
			def InitiatingPipelineId = getAncestor(CurrentPipelineId)
			
			def InitiatingPipelineName = ef.getProperty(propertyName:"/myPipelineRuntime/pipelineName", flowRuntimeId: InitiatingPipelineId).property.value
			println "Found initiating pipeline: ${InitiatingPipelineName} - ${InitiatingPipelineId}"
			ef.setProperty(propertyName: "/myJob/InitiatingPipelineId", value: InitiatingPipelineId)
			ef.setProperty(propertyName: "/myJob/InitiatingPipelineName", value: InitiatingPipelineName)
			
		'''.stripIndent()
		
		step "Detach any runtimes for this pipeline", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			def Release = '$[Release]'
			def ReleaseProject = '$[ReleaseProject]'
			def CurrentPipelineId = '$[/myPipelineRuntime/flowRuntimeId]'
			def InitiatingPipelineId = '$[InitiatingPipelineId]'
			def InitiatingPipelineName = '$[InitiatingPipelineName]'
			
			def PipelineIdToDetach = InitiatingPipelineId
			def PipelineNameToDetach = ef.getProperty(propertyName:"/myPipelineRuntime/pipelineName", flowRuntimeId: InitiatingPipelineId).property.value
			
			def AttachedPipelines = ef.getAttachedPipelineRuns(projectName: ReleaseProject, releaseName : Release).attachedPipelineRunDetail
			AttachedPipelines.each { pipe ->
				def AttachedPipelineName = ef.getProperty(propertyName:"/myPipelineRuntime/pipelineName",flowRuntimeId:pipe.flowRuntimeId).property.value
				def AttachedPipelineId = ef.getProperty(propertyName:"/myPipelineRuntime/flowRuntimeId",flowRuntimeId:pipe.flowRuntimeId).property.value
				if (AttachedPipelineId == PipelineIdToDetach) {
					println "Detaching pipeline: ${PipelineNameToDetach} - ${PipelineIdToDetach}"
					ef.detachPipelineRun(flowRuntimeId:PipelineIdToDetach, projectName: ReleaseProject, releaseName : Release)
				}
			}
			
		'''.stripIndent()
		
		step "Attach this runtime pipeline", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			def Release = '$[Release]'
			def ReleaseProject = '$[ReleaseProject]'
			def CurrentPipelineId = '$[/myPipelineRuntime/flowRuntimeId]'
			def InitiatingPipelineId = '$[InitiatingPipelineId]'
			def InitiatingPipelineName = '$[InitiatingPipelineName]'
			
			println "Attaching pipeline: ${InitiatingPipelineName} - ${InitiatingPipelineId}"
			ef.attachPipelineRun(flowRuntimeId:InitiatingPipelineId, projectName: ReleaseProject, releaseName : Release)
		'''.stripIndent()
		
		step "Create link to release in Summary", command: '''\
			ectool setProperty "/myStageRuntime/ec_summary/Release Portfolio" "<html><a target='_blank' href='#pipeline-run-hierarchy/$[/projects[$[ReleaseProject]]/releases[$[Release]]/releaseId]/release-portfolio-list'>$[ReleaseProject] :: $[Release]</a></html>"
		'''.stripIndent()
		
		step "Create job link to release", command: '''\
			ectool setProperty "/myJob/report-urls/Release Portfolio" "../flow/#pipeline-run-hierarchy/$[/projects[$[ReleaseProject]]/releases[$[Release]]/releaseId]/release-portfolio-list"
		'''.stripIndent()

	} // procedure

	release Release,{
		pipeline "Release Integration",{
			stage "Integration",{
				colorCode = '#ff8040'
			}
			stage "Production",{
				colorCode = '#00ffff'
			}
		}
	}

	pipeline ReleaseCandidate,{
		pipelineRunNameTemplate = 'Release Candidate - $[packageName] - $[/timestamp]'
		formalParameter "packageName"
		stage "QA",{
			task 'Enable Audit Reports', {
				subprocedure = 'Audit Reports'
				subproject = projectName
				taskType = 'PROCEDURE'
			}
		}
		stage "Release",{
			task 'Join Release', {
				actualParameter = [
					Release: Release,
					ReleaseProject: projectName
				]
				subprocedureProject = projectName
				subprocedure = 'Attach Pipeline to Release'
				taskType = 'PROCEDURE'
			} // task
		}
	}

	pipeline "Increment PR approvals",{
		stage "Stage 1",{
			task "Increment PR approval property",{
				actualParameter = [
					'commandToRun': '''\
						echo $[/increment /projects/$[/myPipelineRuntime/projectName]/PRs/$[/javascript JSON.parse(myPipelineRuntime.webhookData).title]]
						ectool setProperty "/projects/$[/myPipelineRuntime/projectName]/PRs/$[/javascript JSON.parse(myPipelineRuntime.webhookData).title]_approver" "$[/javascript JSON.parse(myPipelineRuntime.webhookData).approvedBy]"
					'''.stripIndent()
				]
				taskType = 'COMMAND'
			}
		}
		aclEntry principalName: repo.label, principalType: "serviceAccount", executePrivilege: "allow"
	}
	schedule "Increment PR approvals", {
		pipelineName = "Increment PR approvals"
		actualParameter 'ec_stagesToRun', '["Stage 1"]'

		property 'ec_customEditorData', {
			TriggerFlag = '3'
			ec_maxRetries = '5'
			ec_quietTime = '0'
			ec_runDuplicates = '1'
			ec_webhookBranch = '*'
			ec_webhookEventSource = repo.source
			ec_webhookEventType = "approved"
			ec_webhookRepositoryName = repo.name
			formType = '$[/plugins/ECSCM-Git/project/scm_form/webhook]'
			scmConfig = repo.config
		}
		ec_triggerPluginName = 'ECSCM-Git'
		ec_triggerType = 'webhook'
	} // schedule	
	
	pipeline Commit, {
		stage "Feature Branch",{
			colorCode = '#ff7f0e'
		// Update pipeline runtime name based on webhook
			task "Update pipeline runtime name",{
				actualParameter = [
					'commandToRun': 'ectool setPipelineRunName "$[/javascript JSON.parse(myPipelineRuntime.webhookData).title] - $[/timestamp]"',
				]
				taskType = 'COMMAND'
			}
			task 'Enable Audit Reports', {
				subprocedure = 'Audit Reports'
				subproject = projectName
				taskType = 'PROCEDURE'
			}
		// Jenkins Job1 on feature branch, save artifact
			task "Run Jenkins Job1 on feature branch",{
				actualParameter = [
					'commandToRun': 'echo Running Jenkins Job1 on $[/javascript JSON.parse(myPipelineRuntime.webhookData).sourceBranch]',
				]
				taskType = 'COMMAND'
			}		
		// Task notify PR creator
			task "Notify PR creator",{
				actualParameter = [
					'commandToRun': 'echo Notify $[/javascript JSON.parse(myPipelineRuntime.webhookData).author]',
				]
				taskType = 'COMMAND'
			}
		// Gate PR approvals
			gate 'POST', {
				task 'Wait for PR approval', {
					taskType = 'PROCEDURE'
					subproject = projectName
					subprocedure = 'Add Approver to Summary'
					precondition = '$[/javascript getProperty("/projects/$[/myPipelineRuntime/projectName]/PRs/$[/javascript JSON.parse(myPipelineRuntime.webhookData).title]") > 0]' // >0 corresponds to one approval
					errorHandling = 'stopOnError'
					gateType = 'POST'
				}
			}
		}
		stage "Merge",{
			colorCode = '#2ca02c'
		// Merge feature to master
			task "Merge feature to master",{
				description = '/REST/API/1.0/PROJECTS/{PROJECTKEY}/REPOS/{REPOSITORYSLUG}/PULL-REQUESTS/{PULLREQUESTID}/MERGE?VERSION'
				actualParameter = [
					'commandToRun': 'echo git merge $[/javascript JSON.parse(myPipelineRuntime.webhookData).sourceBranch] master',
				]
				taskType = 'COMMAND'
			}
		// Jenkins Job on master branch
			task "Run Jenkins Job1 on master branch",{
				actualParameter = [
					'commandToRun': 'echo Running Jenkins Job1 on $[/javascript JSON.parse(myPipelineRuntime.webhookData).sourceBranch]',
				]
				taskType = 'COMMAND'
			}	
		// Notify approver
			task "Notify PR approver",{
				actualParameter = [
					'commandToRun': 'echo Notify $[/projects/$[/myPipelineRuntime/projectName]/PRs/$[/javascript JSON.parse(myPipelineRuntime.webhookData).title]_approver]',
				]
				taskType = 'COMMAND'
			}
		// Gate, manual approvals
			task 'Manual Approval', {
				gateType = 'POST'
				notificationTemplate = 'ec_default_gate_task_notification_template'
				taskType = 'APPROVAL'
				approver = [
					'admin',
				]
			}
		}
		stage "Package",{
			colorCode = '#8080ff'
		// Jenkins Job2 on master, save packageName
			task "Run Jenkins Job2 on master branch",{
				actualParameter = [
					'commandToRun': '''\
						echo Running Jenkins Job2 on master
						ectool setProperty /myPipelineRuntime/packageName "$[/javascript JSON.parse(myPipelineRuntime.webhookData).title]-$[/increment /myPipeline/PackageNumber].package"
					'''.stripIndent()
				]
				taskType = 'COMMAND'
			}
		// Notify approver
			task "Notify PR approver",{
				actualParameter = [
					'commandToRun': 'echo Notify $[/projects/$[/myPipelineRuntime/projectName]/PRs/$[/javascript JSON.parse(myPipelineRuntime.webhookData).title]_approver]',
				]
				taskType = 'COMMAND'
			}
		// Create Jira ticket, "Update puppet for package $packageName"
			task "Create Jira ticket",{
				actualParameter = [
					'commandToRun': 'echo Update puppet for package $[/myPipelineRuntime/packageName]',
				]
				taskType = 'COMMAND'
			}
		// Gate, manual approval
			task 'Manual Approval', {
				gateType = 'POST'
				notificationTemplate = 'ec_default_gate_task_notification_template'
				taskType = 'APPROVAL'
				approver = [
					'admin',
				]
			}
		}
		stage "Release Candidate",{
		    colorCode = '#00adee'
		// Trigger Release Cadidate pipeline, pass in $packageName
			task 'Trigger Release Cadidate', {
				actualParameter = [
					'packageName': '$[/myPipelineRuntime/packageName]',
				]
				subpipeline = ReleaseCandidate
				taskType = 'PIPELINE'
				triggerType = 'async'
			}
		}
		
		aclEntry principalName: repo.label, principalType: "serviceAccount", executePrivilege: "allow"
	}
	schedule "$Commit - PR created", {
		pipelineName = Commit
		actualParameter 'ec_stagesToRun', '["Feature Branch","Merge","Package","Release Candidate","Audit Reports"]'

		property 'ec_customEditorData', {
			TriggerFlag = '3'
			ec_maxRetries = '5'
			ec_quietTime = '0'	
			ec_runDuplicates = '1'
			ec_webhookBranch = '*'
			ec_webhookEventSource = repo.source
			ec_webhookEventType = "created"
			ec_webhookRepositoryName = repo.name
			formType = '$[/plugins/ECSCM-Git/project/scm_form/webhook]'
			scmConfig = repo.config
		}
		ec_triggerPluginName = 'ECSCM-Git'
		ec_triggerType = 'webhook'
	} // schedule
} // project
