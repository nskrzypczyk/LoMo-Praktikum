# REST API für Positionsdaten
 
Beinhaltet zunächst folgende Felder:

    * lat: Number
    * long: Number
    * timeStamp: String

## Setup

 1. MongoDB installieren
 2. unter Linux: Ordner in /data/db erstellen, und ```sudo chown [username]:[username] /data/db``` eingeben, damit man entsprechende Rechte hat
 3. ```npm i``` ausführen
 4. mongodb starten mit ```mongod```
 5. Node Server starten mit ```npm start```
 6. Profit