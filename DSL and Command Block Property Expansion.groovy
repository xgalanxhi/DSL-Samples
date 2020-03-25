/*

CloudBees Flow DSL: Workaround for lack of "expand" option to procedure command field

Currently (version 2002.1), there is not option to suppress expansion of property expansion ($[...]). So, if you are attempting to create objects with DSL that is to include runtime expansions, you need to work around this limitation. There are at least three options:
1. Break up the string '$[...]', e.g., '$' + '[...]'
2. Substitution:
	def D = '$'
	"${D}[...]"
3. Store the DSL in a step property and evaluate the property with expand=false. This example illustrates how to do that using both ec-perl and ec-groovy

RFE: https://cloudbees.atlassian.net/browse/CEV-24448

*/

project "DSL Property Expansion", {
	procedure "Test DSL Expansion", {
	// This example illustrates the problem
		step "Property expansion in command block", shell: "ectool evalDsl --dslFile '{0}'",
			command : '''\
				project "Default",{
					procedure "Rendered Procedure - evalDsl shell",{
						step "Contains Expansion",
							// This property will be expanded prematurely (in the context of the outer procedure, not the runtime of this procedure.
							command: 'echo Hello From $[/myProcedure]'
						def D = '$'
						step "Workaround",
							command: "echo Hello From ${D}[/myProcedure] and " + '$' + "[/myStep]"
					}
				}
			'''.stripIndent()
		step "DSL as property ec-groovy",{
			property "DSL", value: '''\
				project "Default",{
					procedure "Rendered Procedure - ec-groovy shell",{
						step "Contains Expansion",
							command: 'echo Hello From $[/myProcedure]'
						step "Value from evalDsl",
							command: "echo Value from evalDsl: ${args.val}"
					}
				}	
			'''.stripIndent()
			command = '''\
				import com.electriccloud.client.groovy.ElectricFlow
				import groovy.json.*
				ElectricFlow ef = new ElectricFlow()
				def DSL=ef.getProperty(propertyName: "/myStep/DSL", expand: false, jobStepId: System.getenv()["COMMANDER_JOBSTEPID"]).property.value
				println DSL
				def Params = JsonOutput.toJson([val: "Value passed in through evalDsl"])
				ef.evalDsl(dsl: DSL, parameters: Params)
			'''.stripIndent()
			shell = "ec-groovy"
		}
		step "DSL as property ec-perl",{
			property "DSL", value: '''\
				project "Default",{
					procedure "Rendered Procedure - ec-perl shell",{
						step "Contains Expansion",
							command: 'echo Hello From $[/myProcedure]'
					}
				}	
			'''.stripIndent()
			command = '''\
				use Data::Dumper;
				use ElectricCommander;
				$ef = ElectricCommander->new();
				my $DSL = $ef->getProperty("/myStep/DSL", {expand=>"0"})->findvalue('//value');
				print $DSL;
				$ef->evalDsl({dsl=>$DSL});
			'''.stripIndent()
			shell = "ec-perl"
		}
	}
}
