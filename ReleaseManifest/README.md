# Release Manifest with parameters

This example illustrates how to drive a pipeline or a release with component versions as parameters.
The models create a snapshot from the supplied component versions and this snapshot is used to run the deploy process(es).

## Installation
1. If you are running in Kubernetes, edit the createResource.sh file to point to the hostname of your default resource
1. Run ./install.sh


## Usage
1. Run the pipe or release in the ReleaseManifest project
1. Enter a new snapshot name (or manifest version)
1. Enter component version
1. \[Run\]
1. Note the pipeline run is set to the snapshot name
1. Examine the Path to Production, QA inventory, etc.

