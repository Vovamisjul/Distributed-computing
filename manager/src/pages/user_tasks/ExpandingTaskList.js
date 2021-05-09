import React from "react";

export default class ExpandingTaskList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            expanded: false
        };
    }

    render() {
        return <div className={this.props.className}>
            <div className="userTasksTitle">{this.props.title}</div>
            {
                this.props.show ?
                    <div>
                        <div className="userTasksCount noSelect" onClick={() => this.setState({expanded: !this.state.expanded})}>
                            {this.props.countText} {this.state.expanded ? 'Click to collapse ↑' : 'Click to expand ↓'}
                        </div>
                        <div className="userTasksList" style={
                            {
                                maxHeight: this.state.expanded ? '450px' : '0',
                                borderTop: this.state.expanded ? null : 'none',
                                borderBottom: this.state.expanded ? null : 'none'
                            }
                        }>
                            {this.props.children}
                        </div>
                    </div> :
                    <div className="noResult">{this.props.absentText}</div>
            }</div>;
    }
}