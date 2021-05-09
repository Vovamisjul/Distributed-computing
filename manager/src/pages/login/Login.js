import React from "react";
import controller from "../../UserController";
import {Redirect} from "react-router-dom";
import "./Login.css"

export default class Login extends React.Component {

    constructor(props) {
        super(props);
        this.username = React.createRef();
        this.password = React.createRef();
        this.login = this.login.bind(this);
    }

    async login(event) {
        event.preventDefault();
        try {
            await controller.login(this.username.current.value, this.password.current.value);
            this.props.history.push("tasks");
        } catch (e) {
            alert("Wrong login or password");
        }
    };

    render() {
        if (controller.isLogged()) {
            return <Redirect to="/tasks"/>;
        }

        return (
            <div>
                <div className="registerContent">
                    <h1>Sign in!</h1>
                    <form onSubmit={this.login}>
                        <input
                            className="registerInput"
                            placeholder="Username"
                            ref={this.username}
                        />
                        <br/>
                        <input
                            className="registerInput"
                            type="password"
                            ref={this.password}
                            placeholder="Password"
                        /><br/>

                        <input className="registerSubmit" type='submit' value="Login"/>
                    </form>
                </div>
            </div>
        )
    }
}