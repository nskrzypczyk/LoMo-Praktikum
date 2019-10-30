import React from "react";
import axios from "axios";
import Card from "./card.component";

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
          <Card item={item} />
        ))}
      </div>
    );
  }
}
