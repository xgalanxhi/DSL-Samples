procedure 'groovylib-helloworld', {
  projectName = 'Default'
  timeLimit = '0'

  step 'getLibSource', {
    subprocedure = 'Clone'
    subproject = '/plugins/EC-Git/project'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
    actualParameter 'config', '/projects/HolyPlayground/pluginConfigurations/git-config'
    actualParameter 'gitRepoFolder', '/tmp/groovy-cd-lib'
    actualParameter 'mirror', 'false'
    actualParameter 'overwrite', 'false'
    actualParameter 'repoUrl', 'https://github.com/holywen-cd/groovy-cd-lib'
    actualParameter 'resultPropertySheet', '/myJob/clone'
    actualParameter 'shallowSubmodules', 'false'
    actualParameter 'submodules', 'false'
  }

  step 'useLib', {
    command = '''import org.holywen.Example
import com.electriccloud.client.groovy.ElectricFlow

ElectricFlow ef = new ElectricFlow()
def example = new Example()
example.helloWorld()'''
    shell = 'ec-groovy -cp /tmp/groovy-cd-lib/src'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }
}