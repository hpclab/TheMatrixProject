:: il comando in fondo al batch, una volta sostituiti i giusti path di installazione del programma al posto di C:\TheMatrix, 
:: lancia il programma MetaXMLCreator per validare i file csv scaricati manualmente nella directory iad
::
:: unico argomento: il nome del dataset da abilitare (senza la desinenza .csv)
::
java -Xms128m -Xmx1024m -XX:MaxPermSize=256m -classpath C:\TheMatrix\TheMatrixB.jar;C:\TheMatrix\lib\Dexter.jar;C:\TheMatrix\lib\Lexter.jar;C:\TheMatrix\lib\Neverlang.jar;C:\TheMatrix\lib\commons-io-2.4.jar;C:\TheMatrix\lib\jopt-simple-4.3.jar;C:\TheMatrix\lib\junit-4.11.jar;C:\TheMatrix\lib\mysql-connector-java-5.1.18.jar;C:\TheMatrix\lib\ojdbc6-11.1.0.7.0.jar;C:\TheMatrix\lib\sqljdbc4.jar;C:\TheMatrix\lib\super-csv-2.1.0.jar it.cnr.isti.thematrix.tools.MetaXmlCreator %1
