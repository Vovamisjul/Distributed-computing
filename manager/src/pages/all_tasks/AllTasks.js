import React from "react";
import controller from "../../UserController";
import "./AllTasks.css"
import Header from "../header/Header";

export default class AllTasks extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tasks: []
        };
    }

    async componentDidMount() {
        try {
            let tasks = await (await controller.getTasks()).json();
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
        return <div>
            <Header/>
            {
                this.state.tasks.length > 0 ?
                    <div
                        className="list">
                        {
                            this.state && this.state.tasks.map((task) => {
                                return <div className="listElement" key={task.id}>
                                    <div className="taskInfoHeader">
                                        <div className="taskInfoName">{task.name}</div>
                                        <div className="taskInfoId">{task.id}</div>
                                    </div>
                                    <div className="taskInfoDescription">{task.description}</div>
                                </div>
                            })
                        }
                    </div> :
                    <div className="noResult">There are no available tasks on server.</div>
            }</div>;
    }
}