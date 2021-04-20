/*

CloudBees SDA CD/RO DSL: Procedure that creates a property structure from JSON

Instructions:
1. Run this DSL code from the command line, DSL Editor or DSLIDE
		Command line: ectool evalDsl --dslFile json2properties.groovy
2. Run the procedure, supply the starting property sheet path and the JSON structure

*/

project "DSL-Samples",{
	procedure "JSON to properties",{
		formalParameter "startingpath", defaultValue: "/myJob/expandedJson"
		formalParameter "json", type: "textarea", defaultValue: '''\
			{
				"a": [
						1,
						2
				],
				"b":"a value",
				"c": {
					"x":1,
					"y": {
						"n":"val",
						"o":4,
						"p":[6,8]
					}
				},
				"d":100
			}
			'''.stripIndent()
		step "Create properties", shell: "ec-groovy", command: '''\
			import groovy.json.JsonSlurper
			import com.electriccloud.client.groovy.ElectricFlow
			ef = new ElectricFlow()
			def startingpath='$[startingpath]'
			def Slurper = new JsonSlurper()
			def json = """\
				$[json]
			""".stripIndent()

			obj = Slurper.parseText(json)

			void scan(org.apache.groovy.json.internal.LazyMap object, String path) {
				object.each { k, v ->
					scan(v, "${path}/${k}")
				}
			}
			void scan(java.util.ArrayList object, String path) {
				object.eachWithIndex { v, i ->
					scan(v, "${path}/${i}")
				}
			}
			void scan(object, String path) {
				println "${path} ${object}"
				ef.setProperty(propertyName: "${path}", value: "${object}")
			}
			scan(obj, startingpath)
		'''.stripIndent()
	} // procedure
} // project