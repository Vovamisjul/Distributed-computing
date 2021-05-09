import controller from "../../UserController";
import React from "react";
import {Link, withRouter} from "react-router-dom";
import "./Header.css"

class Header extends React.Component {

    constructor(props) {
        super(props);
        this.loginout = this.loginout.bind(this);
    }

    loginout() {
        controller.logout();
        this.props.history.push("/");
    }

    render() {
        return <div className="header">
            <div className="headerLinks">
                <nav>
                    <Link to="/tasks" className={this.props.location.pathname === "/tasks" ? "headerCurrentLink" : "headerNotCurrentLink"}>Tasks</Link>
                    <Link to="/manager" className={this.props.location.pathname === "/manager" ? "headerCurrentLink" : "headerNotCurrentLink"}>Manager</Link>
                    <Link to="/running" className={this.props.location.pathname === "/running" ? "headerCurrentLink" : "headerNotCurrentLink"}>Running tasks</Link>
                </nav>
            </div>
            <div className="headerCreds">
                <div className="headerUsername">{window.localStorage.getItem("username") || "Guest"}</div>
                <span className="logout noSelect" onClick={this.loginout}>
                    {window.localStorage.getItem("username") ? "Log out" : "Log in"}
                </span>
            </div>
        </div>
    }
}

export default withRouter(Header);