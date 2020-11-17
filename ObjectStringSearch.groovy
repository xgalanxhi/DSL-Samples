/*

CloudBees CD DSL: Illustrates to do a string search over objects

TODO:
- properties
- other objects
- Use to create an index


*/

def args = [
  SearchString: "test",
  Projects: true,
  Procedures: false,
  Applications: false,
  Pipelines: false,
  Releases: true,
  Resources: false
]

def SearchString = args.SearchString
def FoundObjects = []
getProjects().each { Proj ->
  if (Proj.pluginName) {
    // skip plugins
  } else {
    if (args.Projects) {
      if (Proj.projectName.contains(SearchString)) FoundObjects.push([projectName: Proj.projectName])
    }
    if (args.Procedures) getProcedures(projectName: Proj.projectName).each { Proc ->
      if (Proc.procedureName.contains(SearchString)) FoundObjects.push([projectName: Proj.projectName, procedureName: Proc.procedureName])
    }
    if (args.Applications) getApplications(projectName: Proj.projectName).each { App ->
      if (App.applicationName.contains(SearchString)) FoundObjects.push([projectName: Proj.projectName, applicationName: App.applicationName])
    }
    if (args.Pipelines) getPipelines(projectName: Proj.projectName).each { Pipe ->
      if (Pipe.pipelineName.contains(SearchString)) FoundObjects.push([projectName: Proj.projectName, pipelineName: Pipe.pipelineName])
    }
    if (args.Releases) getReleases(projectName: Proj.projectName).each { Rel ->
      if (Rel.releaseName.contains(SearchString)) FoundObjects.push([projectName: Proj.projectName, releaseName: Rel.releaseName])
    }
  }
}
if (args.Resources) getResources().each { Res ->
  if (Res.resourceName.contains(SearchString) ||  Res.hostName.contains(SearchString) ) FoundObjects.push([resourceName: Res.resourceName, hostName: Res.hostName])
}
FoundObjects