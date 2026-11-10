# StudioCut — progetto Android nativo

Questo repository contiene il codice sorgente di una prima versione Android di StudioCut. La compilazione produce un APK debug tramite GitHub Actions.

## Funzioni della prima versione
- Interfaccia nativa Android in Jetpack Compose.
- Importazione di più fotografie dal selettore Android.
- Timeline fotografica con riordino e rimozione.
- Importazione di una traccia audio locale.
- Esportazione MP4 H.264/AAC e salvataggio in Galleria > Movies/StudioCut.
- Nessun abbonamento, acquisto in-app, credito o servizio AI a pagamento integrato.

## Limiti
Questa è una base iniziale, non un clone completo di CapCut. L'esportazione usa le foto selezionate; non include ancora montaggio delle clip video, titoli, transizioni, animazioni AI o generazione video AI. La build dovrà essere verificata dal workflow prima di considerare l'APK installabile.

## Compilazione
Apri la scheda Actions e avvia "Build StudioCut APK". Al termine, scarica l'artefatto "StudioCut-debug-apk" e poi il file APK contenuto nello ZIP.
