/*

CloudBees CD DSL: Use postp to store command responses to properties

Provided here are a procedure and a pipeline that show how to use the postp capabilities of CloudBees CD
to save command-line outputs to properties. The postprocessor looks for key value pairs that it stores as
properties. These key value pairs are identified by find the = and : symbols.

For the procedure, the properties are stored the job property sheet.
For the pipeline, the properties are stored to the stage summary.

*/

project "Postp",{
	procedure "Use Postp",{
		step "Grab properties from response",{
			description = "Run command and scrape key-value pairs as properties"
			command = '''\
				cat << EOF
				line1 output
				mycount: 12345
				anotherval: abcdef
				Some text and myval=1.1.1.1
				EOF
			'''.stripIndent()
			postProcessor = 'postp --loadProperty /myStep/KeyValue'
			property "KeyValue", value: '''\
				@::gMatchers = (
					{
						id      => "KeyValue",
						pattern => q{([ ]+)\\s*[:=]\\s*([^ ]+)},
						action  => q{
							setProperty("/myJob/" . $1, $2)
						},
					}
				);
			'''.stripIndent()
		}
	}
	pipeline "Use Postp",{
		stage "Stage 1",{
			task "Grab properties from response",{
				actualParameter = [ commandToRun : '''\
						cat << EOF
						line1 output
						mycount: 12345
						anotherval: abcdef
						Some text and myval=1.1.1.1
						EOF
					'''.stripIndent(),
					postp : 'postp --loadProperty /myTask/KeyValue'
				]
				property "KeyValue", value: '''\
					@::gMatchers = (
						{
							id      => "KeyValue",
						pattern => q{([ ]+)\\s*[:=]\\s*([^ ]+)},
							action  => q{
								setProperty("/myStageRuntime/ec_summary/" . $1, $2)
							},
						}
					);
				'''.stripIndent()
				
				subpluginKey = 'EC-Core'
				subprocedure = 'RunCommand'
				taskType = 'COMMAND'
			}
		}
	}
}