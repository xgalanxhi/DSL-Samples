import groovy.transform.BaseScript
import com.electriccloud.commander.dsl.sample.DslBaseScript

// DslBaseScript encapsulates the magic for invoking dsl scripts
@BaseScript DslBaseScript baseScript

File myFile = new File("C:/path/test.txt")
println myFile.getText('UTF-8')

// Examples for using shared scripts and code

// 1. Evaluate basic dsl script
evalDslScript 'scripts/project.dsl'

// 2. Create a pipeline based on a template
createPipelineFromTemplate 'Pipeline project', 'Sample pipeline'

// 3. Add a task based on a template to existing pipeline
addPreGateApprovalTask 'Pipeline project', 'Sample pipeline', 'Stage 1'
