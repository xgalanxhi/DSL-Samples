/*

Cloudbees CD/RO DSL: Use manually uploaded documents in a pipeline

This example illustrates how manually uploaded documents (such as log files or test reports) can be used in a pipeline.

Installation
- Run this DSL code from the DSLIDE

Usage
- Navigate to Artifact management
- Create an artifact
- Upload a file to that artifact location
- Run the pipeline
- When prompted, use the parameters to select the uploaded artifact version
- Select "Completed" and [OK] to allow the pipeline to continue
- Note the following step retrieves the artifact version

*/

project "DSL-Samples",{
	pipeline 'Upload user documents', {
		stage 'Stage 1', {
			colorCode = '#289ce1'

			task 'Upload docs', {
				instruction = 'Go to Artifact management and upload your document, then use the parameters to select that document.'
				notificationEnabled = '1'
				notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
				subproject = 'DSL-Samples'
				taskType = 'MANUAL'
				approver = [
					'Everyone',
				]

				formalParameter 'ArtName', {
					optionsDsl = '''\
						import com.electriccloud.domain.FormalParameterOptionsResult

						def options = new FormalParameterOptionsResult()

						getArtifacts().each { art ->
							options.add(art.artifactName, art.artifactName)
						}
						return options
					'''.stripIndent()
					orderIndex = '1'
					required = '1'
					type = 'select'
				}

				formalParameter 'ArtVer', {
					dependsOn = 'ArtName'
					optionsDsl = '''\
						def options = new FormalParameterOptionsResult()

                        String artName = args.parameters["ArtName"]

                        if (artName) {
							getArtifactVersions(artifactName: args.parameters["ArtName"]).each { ver ->
								options.add(ver.version, ver.version)
							}
						} else {
							getArtifactVersions().each { ver ->
								options.add(ver.version, ver.version)
							}
						}
						return options
					'''.stripIndent()
					orderIndex = '2'
					required = '1'
					type = 'select'
				}
			}
			
			task 'Retrieve the document', {
				taskType = 'PLUGIN'
				subpluginKey = 'EC-Artifact'
				subprocedure = 'Retrieve'
				actualParameter = [
					artifactName: '$[/myStageRuntime/tasks[Upload docs][ArtName]]',
					artifactVersionLocationProperty: '/myJob/retrievedArtifactVersions/$[assignedResourceName]',
					filterList: '',
					overwrite: 'update',
					retrieveToDirectory: '',
					versionRange: '$[/myStageRuntime/tasks[Upload docs][ArtVer]]',
				]
			}
		}
	}
}