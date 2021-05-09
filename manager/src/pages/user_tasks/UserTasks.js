import React from "react";
import RunningTasks from "./RunningTasks";
import QueuedTasks from "./QueuedTasks";
import FinishedTasks from "./FinishedTasks";
import controller from "../../UserController";
import {Redirect} from "react-router-dom";
import CreateTask from "./CreateTask";
import Header from "../header/Header";
import "./UserTasks.css"

export default class UserTasks extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            showCreate: false
        };
    }

    render() {
        if (!controller.isLogged()) {
            return <Redirect to="/login"/>;
        }
        return <div>
            <Header/>
            <QueuedTasks/>
            <RunningTasks/>
            <FinishedTasks/>
            {
                this.state.showCreate && <CreateTask onClose={() => this.setState({showCreate: false})}/>
            }
            <div className="createTask noSelect" onClick={() => this.setState({showCreate: true})}>
                Add new task
            </div>
        </div>;
    }
}