import axios from "./config/axiosConfig.js";

const key = "config"
const configApi = {
  key,

  getConfig: async () => {
    const {data} = await axios.get(`/${key}`, {baseMsg: "Error while fetching user configuration"});
    return data;
  },

  updateTimeConfig: async (body) => {
    const {data} = await axios.patch(`/${key}/time`, {...body}, {baseMsg: "Error while updating time configuration"});
    return data;
  },

  updateJiraConfig: async (body) => {
    const {data} = await axios.patch(`/${key}/jira`, {...body}, {baseMsg: "Error while updating Jira configuration"});
    return data;
  },

  updateExternalServiceConfig: async (body) => {
    const {data} = await axios.patch(`/${key}/externalService`, {...body}, {baseMsg: "Error while updating External Service configuration"});
    return data;
  },
};

export default configApi;