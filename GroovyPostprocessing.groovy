/*

CloudBees CD DSL: Using ec-groovy to perform post processing

Step and process step definitions can include a post processing operation. This can be used to parse the output of the executed command and store the results to properies or used to set the step outcome. The command output which is sent to the log file is piped to the post processing command. The CloudBees CD command 'postp' is designed to be used in this way, where you provide this command some Perl code to do the customer processing. It is possible to use other commands as well. This example illustrates how to use an ec-groovy script to do the processing.

The first step in the example below creates the ec-groovy script. The second step is the one that generates output to be processed. In this example, each line of the command output is store to a job property (line0, line1, ...).


*/

project "Groovy Postprocessing",{
	procedure "Test Postp.groovy",{
		step "Create Postp.groovy",
			subproject : '/plugins/EC-FileOps/project',
			subprocedure : 'AddTextToFile',
			actualParameter : [
				Path: 'Postp.groovy',
				Content: '''\
					import com.electriccloud.client.groovy.ElectricFlow
					ElectricFlow ef = new ElectricFlow()
					System.in.eachLine() { line, number -> 
						ef.setProperty(propertyName: (String) "/myJob/line" + number, value: line)
					}
				'''.stripIndent()
			]
		step "Use Postp.groovy", command: '''\
			cat << EOF
			line1 output
			myval: 12345
			anotherval: abcdef
			Some text and myval=1.1.1.1
			EOF
		'''.stripIndent(), postProcessor: "ec-groovy ./Postp.groovy"
	}
}