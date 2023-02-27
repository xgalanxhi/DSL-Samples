package com.electriccloud.commander.dsl.sample

import org.codehaus.groovy.control.CompilerConfiguration
import com.electriccloud.commander.dsl.DslDelegate
import com.electriccloud.commander.dsl.DslDelegatingScript
import groovy.yaml.YamlSlurper

abstract class DslBaseScript extends DslDelegatingScript {

	/**
	 * Utility method for creating a pipeline based on the pre-defined template
	 */
	def createPipelineFromTemplate(String projectName, String pipelineName) {
		evalDslScript('scripts/pipelineTemplate.dsl',
				[projectName: projectName, pipelineName: pipelineName])
	}

	/**
	 * Utility method for creating a pipeline based on the pre-defined template
	 */

	def createPipelineFromTemplate(String yamlParamFilePath) {
		// Find file in classpath
		InputStream stream = this.scriptClassLoader
				.getResourceAsStream(yamlParamFilePath)
		def yamlFileContent = stream?.text
		println "yamlFileContent: " + yamlFileContent
		evalDslScript('scripts/pipelineTemplate.dsl',
				new YamlSlurper().parseText(yamlFileContent))
	}

	/**
	 * Utility method for adding pre gate approval task to the given
	 * pipeline stage
	 */
	def addPreGateApprovalTask(String projectName, String pipelineName, String stageName) {
		evalDslScript('scripts/pregateApprovalTask.dsl',
				[projectName: projectName, pipelineName: pipelineName, stageName: stageName])
	}

	// boiler-plate code to evaluate dsl scripts from within dsl
	def evalDslScript(String dslFile, def args=[:]) {

		// Find file in classpath
		InputStream stream = this.scriptClassLoader
				.getResourceAsStream(dslFile)
		def dslScript = stream?.text

		CompilerConfiguration cc = new CompilerConfiguration();
		cc.setScriptBaseClass(DelegatingScript.class.getName());
		GroovyShell sh = new GroovyShell(this.scriptClassLoader, cc);
		DelegatingScript script = (DelegatingScript)sh.parse(dslScript)
		script.setDelegate(this.delegate);
		script.binding = new Binding(args: args)
		return script.run();

	}

	/**
	 * Intercept the DslDelegate so it can be set as the delegate on the
	 * dsl scripts being evaluated in context of the parent dsl script.
	 * Before setting the delegate, also capture the script's class loader
	 * before the dslDelegate hijacks the calls. This is needed to get the
	 * reference to the groovy class loader used for evaluating the DSL
	 * script passed in to <code>evalDslScript</code>.
	 */
	private def delegate;
	private def scriptClassLoader;

	void setDelegate(DslDelegate delegate) {
		this.scriptClassLoader = this.class.classLoader
		this.delegate = delegate;
		super.setDelegate(delegate)
	}
    // end boiler-plate


}

