/*

CloudBees CD DSL: Simple Release model to be kicked off by Jenkins

*/

project "Jenkins CD",{
	release "Jenkins initiated Release",{
		pipeline "Release Pipeline",{
			formalParameter "BuildId"
			formalParameter "ComponentVersion"
			
			stage "Stage 1"
		}
	}
}