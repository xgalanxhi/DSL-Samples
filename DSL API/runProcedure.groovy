/*

CloudBees CD/RO DSL: DSL API to run a procedure

*/

project "DSL-Samples",{
	procedure "DSL API test",{
		formalParameter "Input1"
		formalParameter "Input2"
		step "Echo inputs", command: 'echo $[Input1] $[Input2]'
	}
}

def args=[
	projectName: "DSL-Samples",
	procedureName: "DSL API test",
	actualParameter: [
		Input1: "xyz",
		Input2: "abc"
	]
]

runProcedure(args)