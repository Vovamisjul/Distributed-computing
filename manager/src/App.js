import {BrowserRouter as Router, Route, Switch} from 'react-router-dom';
import Login from "./pages/login/Login";
import NotFound from "./pages/not_found/NotFound";
import AllTasks from "./pages/all_tasks/AllTasks";
import RunningTasks from "./pages/running_tasks/RunningTasks";
import UserTasks from "./pages/user_tasks/UserTasks";

function App() {
    return (
        <Router>
            <div>
                <Switch>
                    <Route exact path="/(|login)" component={Login}/>
                    <Route path="/tasks" component={AllTasks}/>
                    <Route path="/running" component={RunningTasks}/>
                    <Route path="/explorer" component={UserTasks}/>
                    <Route component={NotFound}/>
                </Switch>
            </div>
        </Router>
    );
}

export default App;
