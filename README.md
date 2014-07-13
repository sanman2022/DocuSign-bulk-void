DocuSign-bulk-void
==================

Bulk voiding of Envelopes in DocuSign

How to use it
1. Build a list of envelope Ids in an XML format shown in EnvelopeList.xml
2. Supply your credentials-email address, password, Integrator key
3. Adjust the URL for the DocuSign enviroment (Demo/Prod-www/na2) if necessary
4. Compile the java code
5. Adjust the Bulk void reason, if you choose to do so.
6. Run it.


What to expect
1. If the envelope is already in a terminal state there won't be any change
2. If the envelope is in progress it will be voided
3. Appropriate messages on the console will show the progress.


