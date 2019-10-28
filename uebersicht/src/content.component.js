import React from "react";
import axios from "axios";
import ReactTable from "react-table";

export default class Content extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pos: []
    };
  }
  componentDidMount() {
    axios
      .get("http://localhost:80/api/position/all")
      .then(response => {
        console.log("DATA: ", response.data);
        this.setState({
          pos: response.data
        });
      })
      .catch(err => console.log(err));
  }

  render() {
    return (
      <div className="grid">
        {this.state.pos.map(item => (
          <article>
            <div className="timeStamp">
              <span className="timeStamp">Zeitpunkt:</span> {item.timeStamp}
            </div>
            <hr />
            <div>
              <span>Longitude: </span> {item.long}
            </div>
            <div>
              <span>Latitude:</span> {item.lat}
            </div>
            <div>
              <span>Altitude:</span> {item.alt}
            </div>
            <div>
              <span>Acceleration X:</span> {item.accX}
            </div>
            <div>
              <span>Acceleration Y:</span> {item.accY}
            </div>
            <div>
              <span>Acceleration Z:</span> {item.accZ}
            </div>
            <div>
              <span>Proximity [cm]:</span> {item.prox}
            </div>
            <div>
              <span>Rotation X:</span> {item.axisX}
            </div>
            <div>
              <span>Rotation Y:</span> {item.axisY}
            </div>
            <div>
              <span>Rotation Z (Azimuth): </span>
              {item.axisZ}
            </div>
           
          </article>
        ))}
      </div>
    );
  }
}
