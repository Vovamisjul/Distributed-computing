import controller from "../../UserController";
import {withRouter} from "react-router-dom";
import React from "react";
import ExpandingTaskList from "./ExpandingTaskList";

class FinishedTasks extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tasks: []
        };
    }

    async componentDidMount() {
        await this.updateTasks();
    }

    async updateTasks() {
        try {
            let tasks = await (await controller.getUserCompletedTasks()).json();
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
        return <ExpandingTaskList className="finishedTasks" title="Finished tasks" show={this.state.tasks.length > 0} countText={`You have ${this.state.tasks.length} completed tasks.`} absentText="There are no your tasks that has been completed">
            {
                this.state.tasks.map((task) => {
                    return <div className="listElement" key={task.copyId}>
                        <div className="userTaskHeader">
                            <div className="userTaskName">{task.taskInfo ? task.taskInfo.name : "Unknown task type"}</div>
                            <div className="userTaskDate">
                                <div className="userTaskCreated"><b>Created at: </b>{new Date(task.created).toLocaleString()}</div>
                                <div className="userTaskCreated"><b>Finished at: </b>{new Date(task.finished).toLocaleString()}</div>
                            </div>
                        </div>
                        <div className="userTaskParams"><b>Params: </b>{task.params || <i>none</i>}</div>
                        <div className="userTaskParams"><b>Result: </b>{task.result}</div>
                        <div className="userTaskComment">{task.comment}</div>
                    </div>
                })
            }
        </ExpandingTaskList>
    }
}

export default withRouter(FinishedTasks);