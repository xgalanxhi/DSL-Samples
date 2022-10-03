/*

CloudBees CD/RO DSL: Create Git and ScmSyn configurations

With v10.3 first class plugin configurations were introduced as were previews
of SCM sync objects. This example shows how to set up these configurations to
enable synchronizing a Git repository with a CloudBees CD/RO server.

Objects Created
- EC-Git plugin configuration
- scmSync configuration
- trigger git polling configuration

Instructions
- Create a git token that can be used to run APIs against your git provider
	(GitHub, Bitbucket, etc) and set the GitToken variable below to that value
- Create a git repository with CloudBees CD/RO DSL content. This can be DSL
	files at the root of of the repository and or use the "Export DSL" Self-
	service catalog items to generated a DSL directory hierarchy. Fill in the
	value for the Repo variable below.
- Run this DSL to create the objects
	ectool evalDsl --dslFile ScmSync.groovy
- Periodially, you will see a job that runs this EC-Git plugin procedure "Polling".
	This procedure will apply any the code from your repository if there are changes.

*/

def GitToken = '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'
def Repo = 'https://github.com/cb-thunder/GitOps.git'

project "GitOps",{
	pluginConfiguration 'GitHub', {
		field = [
			'authType': 'token',
			'debugLevel': '0',
			'ignoreSSLErrors': 'false',
			'library': 'jgit',
			'repositoryURL': Repo,
			'token_credential': 'token_credential',
		]
		pluginKey = 'EC-Git'
		credential 'token_credential', {
			userName = ''
			password = GitToken
		}
	}
	scmSync "My GitOps",{
		branch = "main"
		configurationName = "GitHub"
		configurationProjectName = projectName
		destinationDir = "tmp"
		repository = Repo
		scmType = "git"
		syncType = "fromScm"
		resourceName = "local"
		trigger "My GitOps trigger",{
			triggerType = "polling"
			pluginKey = "EC-Git"
		}
	}
}

// ACL entry required because repository is creating a new project
// Add ACL entries for any other objects being create or modified by the repo DSL
aclEntry systemObjectName: "projects",
	objectType: 'systemObject',
	principalType : 'user',
	principalName: "project: GitOps",
	modifyPrivilege : 'allow'
