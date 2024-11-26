import axios from "./config/axiosConfig.js";

const syncJiraApi = {
  syncFromJira: async (body) => {
    const {data} = await axios.post(`/sync/jira/from`, {...body}, {baseMsg: "Error while synchronizing from jira"});
    return data;
  },

  syncIntoJira: async (body) => {
    const {data} = await axios.post(`/sync/jira/to`, {...body}, {baseMsg: "Error while synchronizing into jira"});
    return data;
  },

  syncAllWorklogs: async () => {
    await axios.post(`/sync/jira/worklogs`, {}, {baseMsg: "Error while synchronizing worklogs"});
  },

  syncWorklogsForIssue: async (issueKey) => {
    await axios.post(`/sync/jira/worklogs/${issueKey}`, {}, {baseMsg: `Error while synchronizing worklogs for issue ${issueKey}`});
  },

  getProgress: async () => {
    const {data} = await axios.get(`/sync/jira/progress`, {}, {baseMsg: "Error while getting worklogs"});
    return data;
  },
};

export default syncJiraApi;