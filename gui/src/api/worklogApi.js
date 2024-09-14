import axios from "./config/axiosConfig.js";

const key = "work-logs"
const worklogApi = {
  key,
  synchronizeWorklogs: async () => {
    await axios.post(`/${key}`, {}, {baseMsg: "Error while synchronizing worklogs"});
  },
  getProgress: async () => {
    const {data} = await axios.get(`/${key}`, {}, {baseMsg: "Error while getting worklogs"});
    return data;
  }
};

export default worklogApi;