import React from "react";
import axios from "axios";
import Card from "./card.component";
import { Map, Marker, Popup, TileLayer } from "react-leaflet";

export default class Content extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      pos: [10]
    };
    this.position = [51.505, -0.09];
  }

  componentWillMount() {
    axios
      .get("http://localhost:80/api/position/all")
      .then(response => {
        console.log("DATA: ", response.data);
        this.setState({
          pos: response.data,
          zoom: 17
        });
      })
      .catch(err => console.log(err));
    console.log(this.state.pos[0]);
    this.render();
  }

  render() {
    return (
      <div className="Container">
        <Map
          center={
            this.state.pos != undefined
              ? { lat: this.state.pos[0].lat, lng: this.state.pos[0].long }
              : { lat: 0, lng: 0 }
          }
          zoom={this.state.zoom}
          style={{
            height: 400,
            width: "100%",
            overflow: "hidden",
            marginBottom: "1%",
            border: "1px solid #ccc",
            boxShadow: "2px 2px 6px 0px rgba(0, 0, 0, 0.3)"
          }}
        >
          <TileLayer
            attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
            url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"
          />
          {this.state.pos.map(item =>
            item.long != undefined ? (
              <Marker position={{ lat: item.lat, lng: item.long }}>
                <Popup>
                  <span>{item.timeStamp}</span>
                </Popup>
              </Marker>
            ) : null
          )}
        </Map>
        <div className="grid">
          {this.state.pos.map(item => (
            <Card item={item} />
          ))}
        </div>
      </div>
    );
  }
}
