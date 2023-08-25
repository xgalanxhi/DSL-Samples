# DSL-Samples

This is a collection of DSL samples that illustrate how to implement a variety of usecases wtih CloudBees CD.

In general, these samples can be installed in a CloudBees CD instance using either the DSL
IDE or `ectool evalDsl --dslFile <filename>` from the command line. See the comments in the
DSL files for details on their use.


When adding new examples do the following:

1. Create a new directory for the example
2. Use variables at the top of the script to provide any customization that the user might wish to make.  At a minimum provide a variable for the *Project* the code should be installed to.
3. Create a `README.md` based on the template as follows:

```
# <<Sample Name>

## Description

<<Description of the sample>>

### Installing

<<Installation directions>>

## Example

<<Example of running the sample>>
```

4. Make sure to include the copyrigh notice at the top of any code as follows:

```
/*
 Copyright 2023 Cloudbees

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---------------------------------------------------------------------------------

*/
```
