import StatusCodes from 'http-status-codes';
import {Mutex} from 'async-mutex';

let controller;
const mutex = new Mutex();
export default controller = {

    serverUrl: "http://localhost:8080",

    isLogged() {
        return Boolean(window.localStorage.getItem("accessToken"));
    },

    async login(username, password) {
        let response = await this.sendRequest("/user/login", "POST", {
            username,
            password
        });
        if (response.status !== StatusCodes.CREATED) {
            throw new Error(response.status.toString());
        }
        let body = await response.json();
        window.localStorage.setItem("username", username);
        window.localStorage.setItem("accessToken", body.accessToken);
        window.localStorage.setItem("refreshToken", body.refreshToken);
    },

    async getTasks() {
        let response = await this.sendRequest("/tasks/list", "GET");
        if (response.status !== StatusCodes.OK) {
            throw new Error(response.status.toString());
        }
        return response;
    },

    async getRunningTasks() {
        let response = await this.sendAuthRequest("/tasks/running", "GET");
        if (response.status !== StatusCodes.OK) {
            throw new Error(response.status.toString());
        }
        return response;
    },

    async getUserRunningTasks() {
        let response = await this.sendAuthRequest("/queue/running", "GET");
        if (response.status !== StatusCodes.OK) {
            throw new Error(response.status.toString());
        }
        return response;
    },

    async getUserQueuedTasks() {
        let response = await this.sendAuthRequest("/queue/queued", "GET");
        if (response.status !== StatusCodes.OK) {
            throw new Error(response.status.toString());
        }
        return response;
    },

    async getUserCompletedTasks() {
        let response = await this.sendAuthRequest("/queue/completed", "GET");
        if (response.status !== StatusCodes.OK) {
            throw new Error(response.status.toString());
        }
        return response;
    },

    async createTask(taskId, params, comment) {
        let response = await this.sendAuthRequest("/queue/add", "PUT", {
            taskId,
            params,
            comment
        });
        if (response.status !== StatusCodes.OK) {
            throw new Error(response.status.toString());
        }
        return response;
    },

    async sendRequest(route, method, body) {
        return await fetch(this.serverUrl + route, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: body && JSON.stringify(body)
        });
    },

    async sendAuthRequest(route, method, body, retry = false) {
        const release = retry ? () => {} : await mutex.acquire();
        try {
            let response = await fetch(this.serverUrl + route, {
                method: method,
                headers: {
                    "Content-Type": "application/json",
                    "Authorization":  `Bearer ${window.localStorage.getItem("accessToken")}`
                },
                body: body && JSON.stringify(body)
            });
            if (response.status === StatusCodes.UNAUTHORIZED) {
                if (!retry) {
                    await this.updateToken();
                    return await this.sendAuthRequest(route, method, body, true);
                } else {
                    this.logout();
                    throw new Error(StatusCodes.UNAUTHORIZED.toString());
                }
            }
            return response;
        } finally {
            release()
        }
    },

    async updateToken() {
        let response = await this.sendRequest("/user/refreshtoken", "POST", {
            refreshToken: window.localStorage.getItem("refreshToken")
        });
        if (response.status === StatusCodes.UNAUTHORIZED) {
            this.logout();
            throw new Error(StatusCodes.UNAUTHORIZED.toString());
        }
        let body = await response.json();
        window.localStorage.setItem("accessToken", body.accessToken);
        window.localStorage.setItem("refreshToken", body.refreshToken);
    },

    logout() {
        window.localStorage.clear();
    }
}