/*

CloudBees CD/RO DSL: Populate Dropdown menu from URL data

This example illustrates how to create a list of dropdown menu values from a URL.

*/

project "DSL-Samples",{
	procedure 'Parameter Dropdown from URL', {
		formalParameter 'org',
			defaultValue: 'cloudbees',
			orderIndex: 1,
			type: 'entry'
		formalParameter 'repo',
			orderIndex: 2,
			required: true,
			type='select',
			dependsOn: 'org',
			optionsDsl: '''\
				import com.electriccloud.domain.FormalParameterOptionsResult
				def org = args.parameters['org']
				import groovy.json.JsonSlurper
				def options = new FormalParameterOptionsResult()
				def response = '[{"name":"empty"}]'
				try {
					response = (new URL("https://api.github.com/orgs/${org}/repos").text)
				} catch(e) {}
				def Slurper = new JsonSlurper()
				def repos = Slurper.parseText(response)
				repos.each {
					options.add( it.name, it.name)
				}
				return options
			'''.stripIndent()
	}
}