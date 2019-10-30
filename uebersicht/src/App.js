import React from "react";
import logo from "./logo.svg";
import "./App.css";
import Content from "./content.component";

function App() {
  return (
    <div className="App">
      <header className="App-header">Die LOKALISATOREN</header>
      <h1 className="h">Gespeicherte Daten</h1>
      <p className="info">
        Info: Bei Klick auf einen Zeitstempel wird zu Google Maps weitergeleitet
        ;)
      </p>
      <hr />
      <Content />
    </div>
  );
}

export default App;
