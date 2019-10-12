const express = require("express");
var indexRouter = require("./routes/index");
const app = express();
const bodyParser = require("body-parser");
const pos = require("./routes/api/pos");
const mongoose = require("mongoose");

app.use(
  bodyParser.urlencoded({
    extended: false
  })
);
app.use(bodyParser.json());

//Mit MongoDB verbinden
const db = require("./config/keys").mongoURI;
mongoose
  .connect(db, { useNewUrlParser: true })
  .then(() => console.log("MongoDB erfolgreich verbunden!"))
  .catch(err => console.log(err));

app.use("/", indexRouter);
app.use(bodyParser.json());
app.use("/api/position", pos);

const port = process.env.PORT || 80;

app.listen(port, () => console.log(`Server läuft nun auf Port: ${port} !`));
