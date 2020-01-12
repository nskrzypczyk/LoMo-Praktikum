const express = require("express");
const router = express.Router();
const SensorData = require("../../models/Position");
const fs = require('fs');

router.post("/send", (req, res) => {
  console.log(req);
  const newPos = new SensorData({
    timeStamp: req.body.timeStamp,
    long: req.body.long,
    lat: req.body.lat,
    alt: req.body.alt,
  });
  newPos
    .save()
    .then(() => {
      res.json("Daten übermittelt!");
    })
    .catch(err => {
      res.json(err);
    });
});

router.get("/all", (req, res) => {
  SensorData.find({})
    .sort("-timeStamp")
    .then(arr => {
      res.send(arr);
    })
    .catch(err => {
      res.status(500).send({ error: err.message });
    });
});

router.post("/export", (req, res) => {
  console.log(req);

  SensorData.find({})
      .sort("-timeStamp")
      .then(arr => {

        let kmldata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        kmldata += "<kml xmlns=\"http://earth.google.com/kml/2.2\">\n";

        for(let i=0; i<arr.length; i++){
          kmldata += "<Placemark>\n";
          kmldata += "<name>" + arr.timeStamp + "</name>\n";
          kmldata += "<description></description>\n";
          kmldata += "<Point>\n";
          kmldata += "<coordinates>" + arr.long + "," + arr.lat + ",0</coordinates>\n";
          kmldata += "</Point>\n";
          kmldata += "</Placemark>\n";
        }
        kmldata += "</kml>\n";

        fs.writeFile("/kml/" + "Export_" + Date.now(), kmldata, 'utf8', function (err) {
          if (err) {
            res.status(500).send({error: err.message});
          }
          res.json("Daten exportiert");
          console.log("Daten exportiert");
        });
      })
      .catch(err => {
        res.status(500).send({error: err.message});
      });
});

module.exports = router;
