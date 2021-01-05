import groovy.yaml.*
 
def configYaml = '''\
---
pipeline: My Pipeline
- stages: 
  - name: QA
    type: command
	command: echo testing...
  - name: UAT
    type: manual
	approvers: admin
'''

def config = new YamlSlurper().parseText(configYaml)

config.pipeline
