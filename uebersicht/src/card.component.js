import React from "react";

export default class Card extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      item: props.item
    };
  }

  render() {
    return (
      <article>
        <a
          className="timeStamp"
          href={
            "https://www.google.com/maps/search/?api=1&query=" +
            this.state.item.lat +
            "," +
            this.state.item.long
          }
        >
          <span className="timeStamp">Zeitpunkt:</span>{" "}
          {this.state.item.timeStamp}
        </a>
        <hr />
        <div>
          <span>Longitude: </span> {this.state.item.long}
        </div>
        <div>
          <span>Latitude:</span> {this.state.item.lat}
        </div>
        <div>
          <span>Altitude:</span> {this.state.item.alt}
        </div>
        <div>
          <span>Acceleration X:</span> {this.state.item.accX}
        </div>
        <div>
          <span>Acceleration Y:</span> {this.state.item.accY}
        </div>
        <div>
          <span>Acceleration Z:</span> {this.state.item.accZ}
        </div>
        <div>
          <span>Proximity [cm]:</span> {this.state.item.prox}
        </div>
        <div>
          <span>Rotation X:</span> {this.state.item.axisX}
        </div>
        <div>
          <span>Rotation Y:</span> {this.state.item.axisY}
        </div>
        <div>
          <span>Rotation Z (Azimuth): </span>
          {this.state.item.axisZ}
        </div>
      </article>
    );
  }
}
