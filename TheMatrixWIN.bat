:: il comando in fondo al batch, una volta sostituiti i giusti path di installazione del programma al posto di C:\TheMatrix, lancia il test di thematrix
:: nella versione windows non sono ancora presenti le opzioni per cancellare la cache dei file intermedi 
:: previous version
:: java -Xms128m -Xmx1024m -XX:MaxPermSize=256m -classpath C:\TheMatrix;C:\TheMatrix\libs\*;C:\TheMatrix\src;C:\TheMatrix\bin;C:\TheMatrix\mapping it.cnr.isti.thematrix.test.Test
::
:: 1) sostituire C:\TheMatrix con il path di installazione, e modificare i path finali
:: 2) lanciare aggiungendo eventuali altre opzioni, il nome dello script e successivi parametri
:: 3) nota: questa versione accetta al massimo 5 parametri complessivamente
:: 
:: --ignoreDBconnection --dry-run
::
java -Xms128m -Xmx1024m -XX:MaxPermSize=128m -classpath C:\TheMatrix\TheMatrixB.jar;C:\TheMatrix\lib\Dexter.jar;C:\TheMatrix\lib\Lexter.jar;C:\TheMatrix\lib\Neverlang.jar;C:\TheMatrix\lib\commons-io-2.4.jar;C:\TheMatrix\lib\externalsortinginjava-0.1.7.jar;C:\TheMatrix\lib\jopt-simple-4.5.jar;C:\TheMatrix\lib\json-simple-1.1.1.jar;C:\TheMatrix\lib\junit-4.11.jar;C:\TheMatrix\lib\mysql-connector-java-5.1.18.jar;C:\TheMatrix\lib\ojdbc6-11.1.0.7.0.jar;C:\TheMatrix\lib\sqljdbc4.jar;C:\TheMatrix\lib\super-csv-2.1.0.jar;C:\TheMatrix\lib\commons-io-2.4.jar;C:\TheMatrix\lib\jopt-simple-4.5.jar it.cnr.isti.thematrix.scripting.sys.TheMatrixSys --fullIADschema 1 --scriptPath scripts "%1" "%2" "%3" "%4" "%5" "%6" "%7" "%8" "%9"
