import React from "react";
import logo from "./logo.svg";
import "./App.css";
import Content from "./content.component";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        Die LOKALISATOREN <h1 className="h">Gespeicherte Daten</h1>
      </header>
      <link
        rel="stylesheet"
        href="https://unpkg.com/leaflet@1.5.1/dist/leaflet.css"
        integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ=="
        crossorigin=""
      />

      <Content />
    </div>
  );
}

export default App;
