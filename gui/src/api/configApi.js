import axios from "./config/axiosConfig.js";

const key = "config"
const configApi = {
  key,

  getConfig: async () => {
    if (!localStorage.getItem("token")) {
      return;
    }
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

  getJiraInstance: async () => {
    const {data} = await axios.get(`/${key}/jira/jiraInstance`, {baseMsg: "Error while fetching jira instance"});
    return data;
  },

  saveJiraInstance: async (body) => {
    const {data} = await axios.post(`/${key}/jira/jiraInstance`, {...body}, {baseMsg: "Error while saving jira instance"});
    return data;
  },

  deleteJiraInstance: async (id) => {
    await axios.delete(`/${key}/jira/jiraInstance/${id}`, {baseMsg: "Error while deleting jira instance"});
  },

  updateExternalServiceConfig: async (body) => {
    const {data} = await axios.patch(`/${key}/externalService`, {...body}, {baseMsg: "Error while updating External Service configuration"});
    return data;
  },
};

export default configApi;