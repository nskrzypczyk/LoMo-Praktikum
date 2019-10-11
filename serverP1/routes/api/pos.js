const express = require("express");
const router = express.Router();
const Position = require("../../models/Position");

router.post("/send", (req, res) => {
  console.log(req);
  const newPos = new Position({
    timeStamp: req.body.timeStamp,
    long: req.body.long,
    lat: req.body.lat
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
