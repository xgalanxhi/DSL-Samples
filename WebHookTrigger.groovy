/*

CloudBees CD DSL: Trigger a pipeline from a Git repository

Creates the following
- A service account
- A session key
- A pipeline
- A trigger (schedule) that points to the pipeline above
- Outputs the webhook URL

Instructions:
1. Create a Git configuration and set the variable GitConfig to point to it
2. Edit RepoName name below to point to your desired repository
3. Edit ServiceAccountName to be a suitable name
4. Run this DSL
5. Note the URL output; this is what you enter into the git repository webhook URL

*/

def GitConfig = "YourGitConfig"
def RepoName = "YourGitRepo"
def ServiceAccountName = "My Git account"

def CdHostName = getProperty(propertyName: "/server/hostName").value

serviceAccount ServiceAccountName
def SessionId 
def mySessions = getSessions(serviceAccountName: ServiceAccountName)

if(mySessions.isEmpty()){
	SessionId = createSession(serviceAccountName: ServiceAccountName, expirationDate: "2021-12-31").sessionId
} else {
	SessionId = mySessions[0].sessionId
}

def WebHookUrl = "https://${CdHostName}/commander/link/webhookServerRequest?operationId=githubWebhook&pluginConfigName=${GitConfig}&sessionId=${SessionId}"

project 'Triggers', {

	  pipeline 'Git Triggered', {
		stage 'Stage 1', {
	  }

	  schedule 'My Git Trigger', {
		pipelineName = 'Git Triggered'
		timeZone = 'America/New_York'
		actualParameter 'ec_stagesToRun', '["Stage 1"]'

		property 'ec_customEditorData', {
		  TriggerFlag = '3'
		  ec_maxRetries = '5'
		  ec_quietTime = '0'
		  ec_runDuplicates = '1'
		  ec_webhookBranch = 'master'
		  ec_webhookEventSource = 'github'
		  ec_webhookEventType = 'push'
		  ec_webhookRepositoryName = RepoName
		  formType = '$[/plugins/ECSCM-Git/project/scm_form/webhook]'
		  scmConfig = GitConfig
		}
		ec_triggerPluginName = 'ECSCM-Git'
		ec_triggerType = 'webhook'
	  }
	}
}

WebHookUrl