Basic analyzer to move Booking data to Dcs domain

Deployment is done in two separate steps.

1) Pack the contents of the "configuration" directory into a file called dcs-demo-analyzer.asx
This means the two dynamic attributes.
These archives will need to be added along with the dcs-demo-analyzer.asx archive when deploying.

2) Add the (compile it) "dcs-demo-analyzer.jar" to the archive, in the "lib" dir


asxModule.xml is the root file, describing other configuration objects
lib/ contain jar files
DcsDemoAnalyzer defines the analyzer and in what client container it shall be installed in
DcsDemoDynamicAttributes define the attributes that should be added

The file structure of the archive should be:

lib/dcs-demo-analyzer.jar
asxModule.xml
DcsDemoDynamicAttributes.xml
DcsDemoAnalyzer.xml