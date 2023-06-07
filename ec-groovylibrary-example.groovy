procedure 'groovylib-helloworld', {
  projectName = 'Default'
  timeLimit = '0'

  step 'getLibSource', {
    subprocedure = 'Clone'
    subproject = '/plugins/EC-Git/project'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
    actualParameter 'config', '/projects/HolyPlayground/pluginConfigurations/git-config'
    actualParameter 'gitRepoFolder', ''
    actualParameter 'mirror', 'false'
    actualParameter 'overwrite', 'false'
    actualParameter 'repoUrl', 'https://github.com/holywen-cd/groovy-cd-lib'
    actualParameter 'resultPropertySheet', '/myJob/clone'
    actualParameter 'shallowSubmodules', 'false'
    actualParameter 'submodules', 'false'
  }

  step 'getLibSource2', {
    subprocedure = 'Clone'
    subproject = '/plugins/EC-Git/project'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
    actualParameter 'branch', ''
    actualParameter 'commit', ''
    actualParameter 'config', '/projects/HolyPlayground/pluginConfigurations/git-config'
    actualParameter 'depth', ''
    actualParameter 'gitRepoFolder', ''
    actualParameter 'mirror', 'false'
    actualParameter 'overwrite', 'false'
    actualParameter 'pathspecs', ''
    actualParameter 'referenceFolder', ''
    actualParameter 'repoUrl', 'https://github.com/holywen-cd/groovy-cd-lib2'
    actualParameter 'resultPropertySheet', '/myJob/clone'
    actualParameter 'shallowSubmodules', 'false'
    actualParameter 'submodules', 'false'
    actualParameter 'tag', ''
  }

  step 'useLib', {
    command = '''import org.holywen.Example
import org.holywen.Example1
import com.electriccloud.client.groovy.ElectricFlow

ElectricFlow ef = new ElectricFlow()
def example = new Example()
example.helloWorld()
def example1 = new Example1()
example1.helloWorld()'''
    shell = 'ec-groovy -cp groovy-cd-lib/src:groovy-cd-lib2/src'
    timeLimit = '0'
    timeLimitUnits = 'seconds'
  }
}