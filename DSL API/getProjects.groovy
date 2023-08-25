/*

CloudBees CD/RO DSL: List projects


*/

projectList = []
getProjects().each { project ->
	if (!project?.pluginKey) { // Ignore plugin projects
		projectList.push(project.name)
	}
}
projectList 