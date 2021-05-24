/*

CloudBees CD DSL: Simple Pipeline model to be kicked off by Jenkins

*/

project "Jenkins CD",{
	pipeline "Jenkins initiated",{
		formalParameter "BuildId"
		formalParameter "ComponentVersion"
		stage "Stage 1"
	}
}