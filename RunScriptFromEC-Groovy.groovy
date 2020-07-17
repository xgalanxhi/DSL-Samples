/*

CloudBees CD DSL: Running scripts from ec-groovy

The groovy method execute() can be used to run commands against an agent shell. There is not an obvious way to change the directory so the execute() method can find referenced files. This example shows how to manage this by using the pattern:

["sh", "-c", "cd path ; script_to_run", "script", "arguments"].execute()
["cmd", /-c", "cd path && script_to_run", "script", "arguments"].execute()


Note that in this example, resource 'local' is pointing to a Linux agent and 'pchost' to a Windows agent.

*/

project "Default", {
	procedure "Run script from ec-groovy", {

		// Linux
		step 'Create /tmp/test.sh',
			subproject : '/plugins/EC-FileOps/project',
			subprocedure : 'AddTextToFile',
			actualParameter : [
				Content: '''\
					echo "Script input: \$1"
				'''.stripIndent(),
				Path: '/tmp/test.sh',
				Append: '0'
			], resourceName: 'local'
			
		step 'Run ec-groovy /tmp/test.sh', shell: 'ec-groovy', command: '''\
			def sout = new StringBuilder(), serr = new StringBuilder()
			// If test.sh is save to the current workspace, ['sh',"test.sh"].execute()
			def proc = ['sh','-c', ["cd /tmp;sh test.sh","abc"].join(" ")].execute()
			proc.consumeProcessOutput(sout, serr)
			proc.waitForOrKill(1000)
			println "out> $sout err> $serr"
		'''.stripIndent(), resourceName: 'local'
		
		// Windows
		step 'Create c:/Temp/test.bat',
			subproject : '/plugins/EC-FileOps/project',
			subprocedure : 'AddTextToFile',
			actualParameter : [
				Content: '''\
					echo "Script input: %1"
				'''.stripIndent(),
				Path: 'c:/Temp/test.bat',
				Append: '0'
			], resourceName: 'pchost'
		
		step 'Run ec-groovy c:/Temp/test.bat', shell: 'ec-groovy', command: '''\
			def sout = new StringBuilder(), serr = new StringBuilder()
			// If test.sh is save to the current workspace, ['sh',"test.sh"].execute()
			def proc = ["cmd","/c",["cd c:\\\\Temp && test.bat","xyz"].join(" ")].execute()
			proc.consumeProcessOutput(sout, serr)
			proc.waitForOrKill(1000)
			println "out> $sout err> $serr"
		'''.stripIndent(), resourceName: 'pchost'		

	}
}