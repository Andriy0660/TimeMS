import axios from "./config/axiosConfig.js";

const syncApi = {
  syncFromJira: async (body) => {
    const {data} = await axios.post(`/syncJira/from`, {...body}, {baseMsg: "Error while synchronizing from jira"});
    return data;
  },

  syncIntoJira: async (body) => {
    const {data} = await axios.post(`/syncJira/to`, {...body}, {baseMsg: "Error while synchronizing into jira"});
    return data;
  },

  syncAllWorklogs: async () => {
    await axios.post(`/syncJira/syncAllWorklogs`, {}, {baseMsg: "Error while synchronizing worklogs"});
  },

  syncWorklogsForIssue: async (issueKey) => {
    await axios.post(`/syncJira/${issueKey}`, {}, {baseMsg: `Error while synchronizing worklogs for issue ${issueKey}`});
  },

  getProgress: async () => {
    const {data} = await axios.get(`/syncJira/progress`, {}, {baseMsg: "Error while getting worklogs"});
    return data;
  },
};

export default syncApi;