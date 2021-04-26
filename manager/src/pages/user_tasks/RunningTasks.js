import controller from "../../UserController";
import {Redirect, withRouter} from "react-router-dom";
import React from "react";

class RunningTasks extends React.Component {

    async componentDidMount() {
        await this.updateTasks();
        this.timerId = setInterval(() => this.updateTasks(), 5000);
    }

    componentWillUnmount() {
        clearInterval(this.timerId);
    }

    async updateTasks() {
        try {
            let tasks = await (await controller.getUserRunningTasks()).json();
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
        return <div>{
            this.state && this.state.tasks && this.state.tasks.length > 0 ?
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
        }</div>;
    }
}

export default withRouter(RunningTasks);