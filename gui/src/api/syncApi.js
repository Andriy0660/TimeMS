import axios from "./config/axiosConfig.js";

const syncApi = {
  syncFromJira: async (body) => {
    const {data} = await axios.post(`/sync/syncFromJira`, {...body}, {baseMsg: "Error while synchronizing from jira"});
    return data;
  },

  syncIntoJira: async (body) => {
    const {data} = await axios.post(`/sync/syncIntoJira`, {...body}, {baseMsg: "Error while synchronizing into jira"});
    return data;
  },
};

export default syncApi;