# Endevor Procedures

## Description

This is a set of procedures to interact with Endevor running on a mainframe

### Installing

* Edit the groovy files for the functions you want to use and change the default properties to meet your needs as follows:

    * `CurrentProject` - The project 

```
def CurrentProject = 'dslsamples'

project CurrentProject,{
    procedure "endevor_ExecutePackage",{
        projectName = CurrentProjectad
...
...
...
```

## Example

<<Example of running the sample>>