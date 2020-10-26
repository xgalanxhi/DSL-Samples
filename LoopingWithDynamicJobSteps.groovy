/*

CloudBees CD DSL: Implement loop as dynamic job steps

When run, this procedure will create job steps for each loop iteration

*/

project "Dynamic steps",{
	procedure "Unroll loop as job step",{
		step "Loop", shell: "ec-groovy", command: '''\
			import com.electriccloud.client.groovy.ElectricFlow
			ElectricFlow ef = new ElectricFlow()
			
			["a","b"].each {
				ef.createJobStep jobStepName: it, command: "echo ${it}"
			}

		'''.stripIndent()
	}
}