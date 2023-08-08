/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------


ElectricFlow DSL Example: Print out JSON equivalent of property sheet

Instructions:
1. Run this DSL code from the command line, DSL Editor or DSLIDE
		Command line: ectool evalDsl --dslFile prop2json.groovy
2. Run the procedure against the sample property sheet (default parameter value)
	or other property sheet. Make sure to reference the property using XPATH, e.g.,
	/projects/My Project/pipelines/My Pipeline/stages/First Stage/myPropertySheetName

*/

def CurrentProject = 'dslsamples'

project CurrentProject,{
	// Sample property sheet
	property "expandedJson",{
		property 'array1', {
			property '0', value: '1'
			property '1', value: '2'
		}
		property 'sub1', {
			property 'sub2', {
				property 'array1', {
					property '0', value: '6'
					property '1', value: '8'
				}
				value1 = 'val'
				value2 = '4'
			}
			value1 = '1'
		}
		value1 = 'a value'
		value2 = '100'
	}

	
	procedure "properties2json", description: "Print out JSON of property sheet",{
		formalParameter "path", defaultValue: "/myProject/expandedJson",
			required: true, description: "Path to property sheet"

		formalOutputParameter 'jsonProperties', {
			description = 'Property sheets converted to JSON'
		}

		step "Generate JSON from properties", shell: "ec-groovy", description: "See log or Job property PropertyJSON for output",
			command: '''\
				import groovy.json.*
				import com.electriccloud.client.groovy.ElectricFlow
				ElectricFlow ef = new ElectricFlow()
				def PropertyName = ef.getProperty(propertyName: '$[path]').property.propertyName
				def PropertyStucture = ef.getProperties(path: '$[path]', recurse: true, expand: false).propertySheet
				
				def pretty = { obj ->
					return JsonOutput.prettyPrint(JsonOutput.toJson(obj))
				}
				
				//println JsonOutput.prettyPrint(JsonOutput.toJson(PropertyStucture))
				
				def prop2json
				prop2json = { struct ->
					def outStruct = [:]
					struct.each { prop ->
						if (prop?.propertySheet) {
							def propStruct = [:]
							propStruct << prop2json(prop.propertySheet.property)
							outStruct << [(prop.propertyName):propStruct]
						} else {
							outStruct << [(prop.propertyName):prop?.value]
						}
					}
					return outStruct
				}
				
				// println PropertyStucture.property[0].propertySheet.property.getClass() // class java.util.ArrayList
				def OutputDataStructure = [(PropertyName):prop2json(PropertyStucture.property)]
				println pretty(OutputDataStructure)
				ef.setOutputParameter( outputParameterName: "jsonProperties", value: pretty(OutputDataStructure))
			'''.stripIndent()
	} // procedure
} // project

/*

getProperties on "root" returns the edited structure below
{
    "propertySheet": {
        "property": [
            {
                "propertyName": "sub1",
                "propertySheet": {
                    "property": [
                        {
                            "propertyName": "subSub",
                            "propertySheet": {
                                "property": [
                                    {
                                        "propertyName": "prop3",
                                        "value": "890"
                                    }
                                ]
                            }
                        },
                        {
                            "propertyName": "prop1",
                            "value": "123"
                        },
                        {
                            "propertyName": "prop2",
                            "value": "456"
                        }
                    ]
                }
            },
            {
                "propertyName": "sub2",
                "propertySheet": {
                    "property": [
                        {
                            "propertyName": "propA",
                            "value": "abc"
                        }
                    ]
                }
            },
            {
                "propertyName": "rootProp",
                "value": "xyz"
            }
        ]
    }
}

*/
