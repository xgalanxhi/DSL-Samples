/*

CloudBees CD DSL: Generate HTML Report

This example illustrates how a Jira feature report could be redendered which
shows where features are by environment or stage

Approach
Sample data is included in a ec-groovy procedure step that uses the Groovy
library MarkupBuilder to generate an HTML document. The HTML includes styles
and a script. The generated report is linked in the Job Details.

*/

project "DSL-Samples",{
	procedure "Feature Release Status Report",{
		step 'Generate Report', shell: 'ec-groovy',
			command: '''\
				import com.electriccloud.client.groovy.ElectricFlow
				import com.electriccloud.client.groovy.models.ActualParameter
				import javax.script.*
				import static groovy.json.JsonOutput.*

				ElectricFlow ef = new ElectricFlow()

				//ConfigObject TableData = new ConfigObject()

				def Stages = [
					'Planning',
					'Dev',
					'INT',
					'Perf',
					'PROD',
				]


				def JiraData = [
					[
						name: "ABC",
						fixVersion: "2.0",
						features: [
							[
								id: "ABC-1234", tooltip: 'New login function\\nStatus: DONE\\nCreated 2020-10-11',
								stories: [
									[ id: "ABC-1236", stage: "INT", status: "DONE" ],
									[ id: "ABC-1237", stage: "PROD", status: "DONE" ],
								],
							],
							[
								id: "ABC-5432", tooltip: 'Page flipper\\nStatus: DONE\\nCreated 2020-11-19',
								stories: [
									[ id: "ABC-1478", stage: "Planning", status: "WIP" ],
									[ id: "ABC-5434", stage: "Perf", status: "DONE" ],
									[ id: "ABC-5435", stage: "Dev", status: "DONE" ],
									[ id: "ABC-5436", stage: "INT", status: "DONE" ],
									[ id: "ABC-5437", stage: "INT", status: "DONE" ],
								]
							]
						]
					],
					[
						name: "DEF",
						fixVersion: "3.9",
						features: [
							[
								id: "DEF-1234", tooltip: 'New login function\\nStatus: DONE\\nCreated 2020-10-11',
								stories: [
									[ id: "DEF-1235", stage: "Planning", status: "Waiting on ABC-1478" ],
									[ id: "DEF-1236", stage: "INT", status: "DONE" ],
									[ id: "DEF-1237", stage: "PROD", status: "DONE" ],
								],
							],
						]
					]
				]

				ConfigObject Projects = new ConfigObject()
				ConfigObject Features = new ConfigObject()
				ConfigObject TableData = new ConfigObject()
				JiraData.each { project ->
					Projects[project.name]["fixVersion"]=project.fixVersion
					project.features.each { feature ->
						Features[feature.id]["tooltip"] = feature.tooltip
					}
					Stages.each { Stage ->
						project.features.each { feature ->
							def stories = []
							feature.stories.each { story ->
								if (story.stage == Stage) stories.push(story)				
							}
							if (stories) {
								TableData[Stage][project.name][feature.id]=stories
							}
						}
					}
				}
				//println Projects["ABC"]["fixVersion"]
				//println Features["ABC-1234"]["tooltip"]
				//println TableData["Planning"]["ABC"]["ABC-5432"][0].id

				//println prettyPrint(toJson(TableData))

				def fileName = "FeatureReleaseStatusReport.html"
				File directory = new File("artifacts")
				File fh = new File("artifacts" + '/' + fileName)
				if (! directory.exists()) directory.mkdir()
				def writer = new StringWriter()  // html is written here by markup builder
				def markup = new groovy.xml.MarkupBuilder(writer)  // the builder
				markup.html {
					head {
						title ('Feature Release Status Report')
						link(rel:'stylesheet', href:'https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css')
						script( '', src:'https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js' )
						script( '', src:'https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js' )
					}


					style {
						mkp.yieldUnescaped \'\'\'
						.tooltip {
							position: relative;
							display: inline-block;
							border-bottom: 1px dotted black;
						}

						.tooltip .tooltiptext {
							visibility: hidden;
							width: 120px;
							background-color: black;
							color: #fff;
							text-align: center;
							border-radius: 6px;
							padding: 5px 0;

							/* Position the tooltip */
							position: absolute;
							z-index: 1;
						}

						.tooltip:hover .tooltiptext {
							visibility: visible;
						}\'\'\'
					}

					body {
						div ('class': 'container') {
							h2('Feature Release Status Report')
							p('Type something in the input field to search the table for feature ID, environment, component, or version:')
							input(class:'form-control', id:'myInput', type:'text', placeholder:'Search..')
							br()
							table('class': 'table table-bordered table-striped') {

							// Create the Stages Columns 
							thead() {
								tr(style: 'background-color:#d1b3ff') {
									Stages.each { stage ->
										th(stage)
									}
								}
							}

							// Create the Name Columns 
							JiraData.each { project ->

									td(colspan:"5", id:"project", style: 'background-color:#00bfff', "${project.name} - ${Projects[project.name].fixVersion}")  // TODO: change the length of Stages
							
									tr()
									Stages.each { stage ->
										td() {
											TableData[stage][project.name].each { feature ->
												div (style: 'width:100%; background-color:#df80ff; position:relative; padding:5px; border: 1px solid black;') {
													//Features[feature.key]["tooltip"]
													p(feature.key)
												}

												TableData[stage][project.name][feature.key].each { story ->
													div (style: 'width:100%; background-color:#f9e6ff; position:relative; padding:5px; border: 1px solid black;') {
														if (story.status == "DONE") {
															p(" - ${story.id}")
														} else {
															p(" - ${story.id} : ${story.status}")
														}
													} 
												}
												p()

											}
										}
									} 
									tr()
								} 
							}
						} // div

						script {
							mkp.yieldUnescaped \'\'\'
							$(document).ready(function(){
								$("#myInput").on("keyup", function() {
									var value = $(this).val().toLowerCase();
									$("#myTable tr").filter(function() {
										$(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
										});
										});
										});\'\'\'
						}
					}
				} // markup

				fh << writer.toString()

				ef.setProperty(
					propertyName: "/myJob/report-urls/Report Data",
					value: "/commander/jobSteps/$[/myJobStep/jobStepId]/${fileName}"
					)
				ef.setProperty(
					propertyName: "/server/ec_ui/flowMenuExtension",
					value: """<?xml version="1.0" encoding="UTF-8"?><menu><tab><label>Deploy Report</label><url>../commander/jobSteps/$[/myJobStep/jobStepId]/${fileName}</url></tab></menu>"""
					)
		'''.stripIndent()
	}
}