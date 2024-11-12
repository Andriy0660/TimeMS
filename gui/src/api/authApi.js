import axios from "./config/axiosConfig.js";

const key = "auth"
const worklogApi = {
  key,

  signUp: async (body) => {
    const {data} = await axios.post(`/${key}/signUp`, {...body}, {baseMsg: "Error while creating an account"});
    return data;
  },

  logIn: async (body) => {
    const {data} = await axios.post(`/${key}/logIn`, {...body}, {baseMsg: "Error during login"});
    return data;
  },

};

export default worklogApi;