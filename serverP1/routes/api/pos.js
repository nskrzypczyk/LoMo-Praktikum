const express = require("express");
const router = express.Router();
const SensorData = require("../../models/Position");

router.post("/send", (req, res) => {
  console.log(req);
  const newPos = new SensorData({
    timeStamp: req.body.timeStamp,
    long: req.body.long,
    lat: req.body.lat,
    alt: req.body.alt,
    accX: req.body.accX,
    accY: req.body.accY,
    accZ: req.body.accZ,
    prox: req.body.prox,
    axisX: req.body.axisX,
    axisY: req.body.axisY,
    axisZ: req.body.axisZ
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

module.exports = router;
