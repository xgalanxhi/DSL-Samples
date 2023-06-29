/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

CloudBees SDA CD/RO DSL: Procedure that creates a property structure from JSON

Instructions:
1. Run this DSL code from the command line, DSL Editor or DSLIDE
		Command line: ectool evalDsl --dslFile json2properties.groovy
2. Run the procedure, supply the starting property sheet path and the JSON structure

*/

def CurrentProject = 'dslsamples'

project CurrentProject,{
	procedure "json2properties",{
		formalParameter "startingpath", defaultValue: "/myProject/expandedJson"
		formalParameter "json", type: "textarea", defaultValue: '''\
			{
				"array1": [
						1,
						2
				],
				"value1":"a value",
				"sub1": {
					"value1":1,
					"sub2": {
						"value1":"val",
						"value2":4,
						"array1":[6,8]
					}
				},
				"value2":100
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