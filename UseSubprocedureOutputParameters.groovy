/*

CloudBees DSL: Using subprocedure ouptput parameters

Output parameters are a handy way to avoid property name clashes and are generally a good practice for generating procedure output. This example illustrate how to call a subprocedure and use its output parameters.

*/

project "DSL-Samples", {
	procedure "Sub", {
		formalOutputParameter "Out"
		step "Save to output parameter", 
			command : "ectool setOutputParameter Out 1234"
	}
	procedure "Top",{
		step "Call Sub", subprocedure: "Sub", subproject: projectName
		step "Use Out", command: 'echo $[/myJob/jobSteps[Call Sub]/outputParameters/Out]'
	}
}