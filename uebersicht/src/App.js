import React from "react";
import logo from "./logo.svg";
import "./App.css";
import Content from "./content.component";

function App() {
  return (
    <div className="App">
      <header className="App-header">LOKALISATOREN</header>
      <h1 className="Content">Gespeicherte Daten</h1>
      <Content />
    </div>
  );
}

export default App;
