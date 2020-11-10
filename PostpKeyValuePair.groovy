/*

CloudBees CD DSL: Use postprocessor (postp) to store command responses to properties

Creates a procedure and a pipeline that illustrate how to use the postprocessor capabilities of CloudBees CD
to save command-line outputs to properties. The postprocessor looks for key value pairs that it stores as
properties. These key value pairs are identified by finding = and : separator symbols.

For the procedure, the properties are stored the job property sheet
For the pipeline, the properties are stored to the stage summary

It is also possible to use the shell to save properties using ectool. Pipe the output of the command to the
following to do something similar to what postp is doing:

	while read -r k v;do if [ -n "$v" ]; then ectool setProperty /myJob/$(echo $k|xargs) --value $(echo $v|xargs); fi;done
	
BTW, the xargs is used here to trim white spaces off the property name and the value

*/

def KeyValuePP = '''\
	@::gMatchers = (
		{
			id      => "KeyValue",
			pattern => q{([^ ]+)\\s*[:=]\\s*(.*)},
			action  => q{
				# /javascript returns "myStageRuntime/ec_summary" if run from a pipeline, "myJob" otherwise
				setProperty("/$[/javascript getProperty("/myPipelineRuntime")?"myStageRuntime/ec_summary":"myJob"]/" . $1, $2)
			},
		}
	);
'''.stripIndent()

// Simulate output from a command
def CommandBlock = '''\
	cat << EOF
	testresult: PASS
	coverage = 95%
	testname : Total System Integration
	EOF
'''.stripIndent()

project "Postp",{
	property "KeyValuePP", value: KeyValuePP
	procedure "Use Postp",{
		step "Grab properties from response",{
			description = "Run command and scrape key-value pairs as properties"
			command = CommandBlock
			postProcessor = 'postp --loadProperty "/myProject/KeyValuePP"'
		}
	}
	pipeline "Use Postp",{
		stage "Stage 1",{
			task "Grab properties from response",{
				actualParameter = [
					commandToRun : CommandBlock,
					postp : 'postp --loadProperty "/projects/$[/myPipelineRuntime/projectName]/KeyValuePP"'
				]
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}
	}
}