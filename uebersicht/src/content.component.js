import React from "react";
export default class Content extends React.Component {
  constructor(props) {
    super.props();
    this.state = {
      data: []
    };
  }
  getData() {
    axios
      .get("http://localhost:80/api/position/getall")
      .then(response => console.log(response));
  }
  render() {
    return <div className="Content">Hello, {this.props.name}</div>;
  }
}
