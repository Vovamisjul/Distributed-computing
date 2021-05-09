import React from "react";
import controller from "../../UserController";
import {Redirect} from "react-router-dom";
import Header from "../header/Header";

export default class RunningTasks extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tasks: []
        };
    }

    async componentDidMount() {
        try {
            let tasks = await (await controller.getRunningTasks()).json();
            this.setState({
                tasks: tasks
            });
        } catch (e) {
            if (e.message === "401") {
                this.props.history.push("/");
            }
        }
    }

    render() {
        if (!controller.isLogged()) {
            return <Redirect to="/login"/>;
        }
        return <div>
            <Header/>
            {
                this.state.tasks.length > 0 ?
                    <div
                        className="list">
                        {
                            this.state && this.state.tasks.map((task) => {
                                return <div className="listElement">
                                    <p>{task.copyId}</p>
                                    <p>{task.author}</p>
                                    <p>{task.taskInfo.id}</p>
                                    <p>{task.taskInfo.name}</p>
                                    <p>{task.taskInfo.description}</p>
                                </div>
                            })
                        }
                    </div> :
                    <div className="noResult">There are no conversations - create a new one, or wait for the
                        invite</div>
            }
        </div>;
    }
}