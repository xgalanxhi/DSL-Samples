/*

ectool evalDsl --dslFile debugging.groovy; cat /tmp/debug.log

Illustrates how to write out data to a file with DSL
Apparently, .exectute() operations are no longer allowed

*/

//"rm -f /tmp/debug.log".execute()
def logfile= new File('/tmp/debug.log')

def me = getProperty(propertyName:"/myUser/userName").value
def time = getProperty(propertyName:"/timestamp HH:mm").value

transaction {
  logfile << "${me} - ${time}: some logging data"
	logfile << "\n"
}
