import StatusCodes from 'http-status-codes';

let controller;
export default controller = {

    serverUrl: "http://localhost:8080",

    isLogged() {
        return Boolean(window.localStorage.getItem("accessToken"));
    },

    async login(username, password) {
        let response = await this.sendRequest("/user/login", "POST", {
            username: username,
            password: password
        });
        if (response.status !== StatusCodes.CREATED) {
            throw new Error(response.status.toString());
        }
        let body = await response.json();
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