import controller from "../../UserController";
import React from "react";
import Select from 'react-select';
import x from "./x.png";

export default class CreateTask extends React.Component {

    constructor(props) {
        super(props);
        this.addTask = this.addTask.bind(this);
        this.taskChose = this.taskChose.bind(this);
        this.state = {
            tasks: [],
            taskParamDesc: [],
            taskParamValues: [],
            isLoading: true,
            comment: ""
        }
    }

    async componentDidMount() {
        try {
            this.setState({
                tasks: await (await controller.getTasks()).json(),
                isLoading: false
            })
        } catch (e) {
            if (e.message === "401") {
                this.props.history.push("/");
            }
        }
    }

    taskChose(option) {
        this.taskId = option.value;
        let task = this.state.tasks.find(task => task.id == option.value);
        this.setState({
            taskParamDesc: task.paramsDescription,
            taskParamValues: new Array(task.paramsDescription.length).fill("")
        })
    }

    async addTask() {
        if (!this.taskId) {
            alert("Choose task, please")
            return;
        }
        await controller.createTask(this.taskId, this.state.taskParamValues, this.state.comment);
        alert("Created successfully")
        this.props.onClose();
    }

    render() {
        return <div className="addTaskModal">
            <div className="addTaskModalHeader">
                <span>Create task</span>
                <img src={x} alt="close" onClick={this.props.onClose}/>
            </div>
            <div className="createTaskInner">
                <div className="createTaskParams">
                    <Select className="createTaskSelect" isLoading={this.state.isLoading} isSearchable={true} onChange={this.taskChose}
                            options={this.state.tasks.map(task => {
                                return {
                                    value: task.id,
                                    label: task.name
                                }
                            })}/>
                    <table className="createTaskTable">
                        <tbody>
                        {
                            this.state.taskParamDesc.map((description, i) => {
                                return <tr key={i}>
                                    <td><label className="paramDescription">{description}: </label></td>
                                    <td><input className="paramInput" value={this.state.taskParamValues[i]} onChange={e => this.setState(state => {
                                        let newValues = [...state.taskParamValues];
                                        newValues[i] = e.target.value;
                                        return {
                                            taskParamValues: newValues
                                        }
                                    })}/></td>
                                </tr>
                            })
                        }
                            <tr key="comment">
                                <td><label className="paramDescription">Comment: </label></td>
                                <td><input className="paramInput" value={this.state.comment} onChange={e => this.setState({comment: e.target.value})}/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div className="createTaskButtons">
                    <div className="createTaskButton noSelect" onClick={this.addTask}>Create</div>
                    <div className="createTaskButton noSelect" onClick={this.props.onClose}>Cancel</div>
                </div>
            </div>
        </div>
    }
}