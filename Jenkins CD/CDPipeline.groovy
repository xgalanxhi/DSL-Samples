project "Jenkins CD",{
	pipeline "Jenkins initiated",{
		formalParameter "BuildId"
		formalParameter "ComponentVersion"
		stage "Stage 1"
	}
}