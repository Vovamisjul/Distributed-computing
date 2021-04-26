import React from "react";
import controller from "../../UserController";

export default class AllTasks extends React.Component {

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
        return <div>{
            this.state && this.state.tasks && this.state.tasks.length > 0 ?
                <div
                    className="list">
                    {
                        this.state && this.state.tasks.map((task) => {
                            return <div className="listElement">
                                <p>{task.id}</p>
                                <p>{task.name}</p>
                                <p>{task.description}</p>
                            </div>
                        })
                    }
                </div> :
                <div className="noResult">There are no conversations - create a new one, or wait for the
                    invite</div>
            }</div>;
    }
}