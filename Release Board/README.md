# Release Board

This project illustrates how a self service catalog can be implemented to manage versioned application packages. The application packages include a list of components and their versions as well as a status. The status can be used to filter down list of application at deploy time.


## Instructions

1. Run the DSL initialize.groovy to create a dummy application and some packages. Examine the application properties. Note the application version, the components and status.
1. Run the DSL AddVersionSSC, SetPackageState.groovy and DeployApp.groovy to create the catalog items
1. Use "Add or Modify Package" to define a new packages
1. Use "Set package stage" to set the package status
1. Use "Deploy Application" to see how the packages can be selected based on status

