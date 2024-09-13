import axios from "./config/axiosConfig.js";

const key = "work-logs"
const worklogApi = {
  key,
  synchronizeWorklogs: async () => {
    await axios.post(`/${key}`, {}, {baseMsg: "Error while synchronizing worklogs"});
  }
};

export default worklogApi;