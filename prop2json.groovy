/*

ElectricFlow DSL Example: Print out JSON equivalent of property sheet

Instructions:
1. Run this DSL code from the command line, DSL Editor or DSLIDE
		Command line: ectool evalDsl --dslFile prop2json.groovy
2. Run the procedure against the sample property sheet (default parameter value)
	or other property sheet. Make sure to reference the property using XPATH, e.g.,
	/projects/My Project/pipelines/My Pipeline/stages/First Stage/myPropertySheetName

*/


project "Properties2JSON",{
	// Sample property sheet
	property "root",{
		property "rootProp", value: "xyz"
		property "sub1",{
				property "prop1", value: "123"
				property "prop2", value: "456"
			property "subSub",{
				property "prop3", value: "890"
			}
		}
		property "sub2",{
			property "propA", value: "abc"
		}
	}
	
	procedure "prop2json", description: "Print out JSON of property sheet",{
		formalParameter "path", defaultValue: "/projects/Properties2JSON/root",
			required: true, description: "Path to property sheet"
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
							propStruct = [:]
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
				ef.setProperty(propertyName: "/myJob/PropertyJSON", value: pretty(OutputDataStructure))
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
