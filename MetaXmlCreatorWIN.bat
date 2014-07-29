:: il comando in fondo al batch, una volta sostituiti i giusti path di installazione del programma al posto di C:\TheMatrix, 
:: lancia il programma MetaXMLCreator per validare i file csv scaricati manualmente nella directory iad
::
:: unico argomento: il nome del dataset da abilitare (senza la desinenza .csv)
::
java -Xms128m -Xmx1024m -XX:MaxPermSize=256m -classpath C:\TheMatrix\TheMatrixB.jar;C:\TheMatrix\libs\Dexter.jar;C:\TheMatrix\libs\Lexter.jar;C:\TheMatrix\libs\Neverlang.jar;C:\TheMatrix\libs\commons-io-2.4.jar;C:\TheMatrix\libs\jopt-simple-4.3.jar;C:\TheMatrix\libs\junit-4.11.jar;C:\TheMatrix\libs\mysql-connector-java-5.1.18.jar;C:\TheMatrix\libs\ojdbc6-11.1.0.7.0.jar;C:\TheMatrix\libs\sqljdbc4.jar;C:\TheMatrix\libs\super-csv-2.1.0.jar it.cnr.isti.thematrix.tools.MetaXmlCreator %1
