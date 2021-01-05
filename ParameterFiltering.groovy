/*

CloudBees CD DSL: Parameter filtering

CloudBees CD pull down parameters can be dynamically populated using DSL.
Currently, there is not a built-in way to filter the generated list from from
the UI. This example shows how to create a large pull down list and a filter.

*/

project 'DSL-Samples',{
	procedure 'Big pick list with filtering', {
	  formalParameter 'Filter', defaultValue: null, {
		orderIndex = '1'
		required = '0'
		type = 'entry'
	  }

	  formalParameter 'FilteredList', defaultValue: null, {
		dependsOn = 'Filter'
		optionsDsl = '''\
			import com.electriccloud.domain.FormalParameterOptionsResult
			def options = new FormalParameterOptionsResult()
			def List = []
			(1..1000).each {
			  List.push("Item ${it}")
			}
			def FilterValue = args.parameters['Filter']
			List.each {
				if (FilterValue) {
					if (it.contains(FilterValue)) options.add(it,it)
				} else {
					options.add(it,it)
				}
			}
			return options
		'''.stripIndent()
		orderIndex = '2'
		//renderCondition = '${Filter} != ""' // This render condition can be used to only show the pull down if a value has been provided in the Filter field.
		required = '0'
		type = 'select'
	  }
	}
}