# FileNet P8 HealthCheck Utility

This utility performs lower level functional testing on FileNet P8 Content Platform Engine object stores and alerts if any issues are encountered.  It is intended to be invoked as a cron job at regular intervals.

Each operation is timed and written to a .csv file, which we can send out metrics on.

Supported operations:
* Connection to P8 Domain
* Document search (each Object Store)
* Document upload (each Object Store)
* Document deletion (each Object Store)
* Identify expired queueitem entries (each Object Store)

The utility requires the references to the FileNet P8 Content Platform Engine API, including:

* listener.jar
* Jace.jar
* log4j.jar
* stax-api.jar
* xlxpScanner.jar
* xlxpScannerUtils.jar
* javax.mail.jar

The core of the project resides in HealthCheck.java.  The encryption piece in AESencrp.java is taken from [here.](http://www.code2learn.com/2011/06/encryption-and-decryption-of-data-using.html)

