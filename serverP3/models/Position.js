const mongoose = require("mongoose");
const Schema = mongoose.Schema;

const SensorData = Schema({
  timeStamp: {
    type: String,
    required: true
  },
  lat: {
    type: Number,
    required: true
  },
  long: {
    type: Number,
    required: true
  },
  alt: {
    type: Number,
    required: true
  },
  text: {
    type: String,
    required: true
  },
});

module.exports = mongoose.model("position", SensorData);
