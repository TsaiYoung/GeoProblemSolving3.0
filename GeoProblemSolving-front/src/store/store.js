import Vue from 'vue'
import Vuex from 'vuex'
Vue.use(Vuex)

export default new Vuex.Store({
    // strict:true,
    state: {
        //data
        userInfo: {
            userState: false,
            userName: 'Visitor',
            userId: '',
            avatar: '',
        },
        projectImg: '',
        activityTree:[],
        IP_Port: window.location.host,
        // IP_Port:"172.21.213.185:8080",
        // IP_Port:"localhost:8080",
        // IP_Port:"172.21.212.72:8082",
        // IP_Port:"94.191.49.160:8080",
    },
    getters: {
        userState: state => {
            return state.userInfo.userState;
        },
        userName: state => {
            return state.userInfo.userName;
        },
        userId: state => {
            return state.userInfo.userId;
        },
        avatar: state => {
            return state.userInfo.avatar;
        },
        userInfo: state => {
            return state.userInfo;
        },
        activityTree: state => {
            return state.activityTree;
        }
    },
    mutations: {
        getUserInfo: state => {
            if (!state.userInfo.userState) {
                $.ajax({
                    url: "/GeoProblemSolving/user/state",
                    type: "POST",
                    async: false,
                    success: function (data) {
                        if (data) {
                            var userInfo = data;
                            userInfo.userState = true;
                            state.userInfo = userInfo;
                        }
                    },
                    error: function (err) {
                        console.log("Get user info fail.");
                    }
                });
            }
        },
        userLogin: (state, data) => {
            let userInfo = data;
            userInfo.userState = true;
            state.userInfo = userInfo;
            sessionStorage.setItem("userInfo", JSON.stringify(state.userInfo));
        },
        userLogout: (state) => {
            state.userInfo = {
                userState: false,
                userName: 'Visitor',
                userId: '',
                avatar: '',
            };
            state.project = {};
            state.subProject = {};
            sessionStorage.removeItem("userInfo");
        },
        uploadAvatar: (state, avatar) => {
            state.userInfo.avatar = avatar;
        },
        setUserInfo: (state, userInfo) => {
            state.userInfo = userInfo;
        },
        setActivityTree: (state, activityTree) => {
            state.activityTree = activityTree;
        }
    }
})
