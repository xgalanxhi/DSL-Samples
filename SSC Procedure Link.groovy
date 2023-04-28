/*

CDRO DSL: Procedure to implement SSC Item and link to created object

*/

project "SSC link", {
	procedure "Create pipeline", {
		step "Apply DSL to create pipeline", shell : 'ectool evalDsl --dslFile',
			command : '''\
				def baseUrl = "https://" + getProperty(propertyName:"/server/settings/ipAddress").value
				def pipelineId = pipeline("Generated pipeline", projectName: "Default").pipelineId
				def url = baseUrl + "/flow/#pipelines/" + pipelineId
				property "/myJob/report-urls/Created pipeline", value: url
			'''.stripIndent()
	}
	catalog 'SSC Link', {
		iconUrl = null
		catalogItem 'Create Procedure', {
			description = '''\
				<xml>
					<title></title>
					<htmlData>
						<![CDATA[
						]]>
					</htmlData>
				</xml>
			'''.stripIndent()
			allowScheduling = '0'
			buttonLabel = 'Create'
			dslParamForm = null
			dslString = null
			endTargetJson = null
			iconUrl = 'icon-catalog-item.svg'
			subpluginKey = null
			subprocedure = 'Create pipeline'
			subproject = projectName
			templateObjectType = 'none'
			useFormalParameter = '0'
		}
	}
}
