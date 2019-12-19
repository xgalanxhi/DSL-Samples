/*

CloudBees Flow DSL: How to save the content of a job step log to a property

*/

project "Logs",{
	procedure "Logs",{
		step "Log to property",{
			command = '''\
				echo line1
				echo line2 etc
			'''.stripIndent()
			postProcessor = "postp --loadProperty /myStep/saveToProperty"
			property "saveToProperty", value: '''\
				sub postpEndHook() {
					my $myLog = "";
						for my $line (@::gLogWindow) {
							$myLog = $line . $myLog;
						}
						setProperty("/myJob/$[/myJobStep/stepName].log", $myLog);
					}
			'''.stripIndent()
		}
	}
}