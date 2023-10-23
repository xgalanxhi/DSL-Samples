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


If you would like to watch a video on the pipeline running you can check out the Youtube link  bellow

[![alt text](https://i9.ytimg.com/vi/zwUu3e3TgFo/mqdefault.jpg?sqp=CIS42qkG-oaymwEmCMACELQB8quKqQMa8AEB-AG4B4AC0AWKAgwIABABGBMgPih_MA8=&rs=AOn4CLBybUbsCpsFrYrfRgk3bbqJuc-pEQ)](https://youtu.be/zwUu3e3TgFo "Youtube Video")