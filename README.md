##DocuSign-bulk-void
==================
Bulk voiding of Envelopes in DocuSign

###How to use it
* Build a list of envelope Ids in an XML format shown in EnvelopeList.xml
* Supply your credentials-email address, password, Integrator key
* Adjust the URL for the DocuSign enviroment (Demo/Prod-www/na2) if necessary
* Compile the java code
* Adjust the Bulk void reason, if you choose to do so.
* Run it.


###What to expect
* If the envelope is already in a terminal state there won't be any change
* If the envelope is in progress it will be voided
* Appropriate messages on the console will show the progress.


