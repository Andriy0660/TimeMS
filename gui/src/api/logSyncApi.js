import axios from "./config/axiosConfig.js";

const logSyncApi = {
  syncFromJira: async (body) => {
    const {data} = await axios.post(`/logsync/syncFromJira`, {...body}, {baseMsg: "Error while synchronizing from jira"});
    return data;
  },

  syncIntoJira: async (body) => {
    const {data} = await axios.post(`/logsync/syncIntoJira`, {...body}, {baseMsg: "Error while synchronizing into jira"});
    return data;
  },
};

export default logSyncApi;