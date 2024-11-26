import axios from "./config/axiosConfig.js";

const key = "auth"
const worklogApi = {
  key,

  signUp: async (body) => {
    const {data} = await axios.post(`/${key}/signup`, {...body}, {baseMsg: "Error while creating an account"});
    return data;
  },

  logIn: async (body) => {
    const {data} = await axios.post(`/${key}/login`, {...body}, {baseMsg: "Error during login"});
    return data;
  },

  logInWithGoogle: async (body) => {
    const {data} = await axios.post("auth/google/login", {...body}, {baseMsg: "Error during login with google"});
    return data;
  },

  logOut: async (body) => {
    await axios.post(`/${key}/logout`, {...body}, {baseMsg: "Error during logout"});
  },

};

export default worklogApi;