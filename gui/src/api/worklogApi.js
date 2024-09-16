import axios from "./config/axiosConfig.js";

const key = "work-logs"
const worklogApi = {
  key,
  synchronizeWorklogs: async () => {
    await axios.post(`/${key}`, {}, {baseMsg: "Error while synchronizing worklogs"});
  },

  synchronizeWorklogsForIssue: async (issueKey) => {
    await axios.post(`/${key}/${issueKey}`, {}, {baseMsg: `Error while synchronizing worklogs for issue ${issueKey}`});
  },

  getProgress: async () => {
    const {data} = await axios.get(`/${key}`, {}, {baseMsg: "Error while getting worklogs"});
    return data;
  }
};

export default worklogApi;