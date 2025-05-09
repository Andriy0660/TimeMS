import axios from "./config/axiosConfig.js";

const key = "work-logs"
const worklogApi = {
  key,
  list: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}`, {params, baseMsg: "Error while fetching not synced worklogs"});
    return data.items;
  },

  create: async (body) => {
    const {data} = await axios.post(`/${key}`, {...body}, {baseMsg: "Error while creating worklog"});
    return data;
  },

  delete: async (issueKey, id) => {
    await axios.delete(`/${key}/${issueKey}/${id}`, {baseMsg: "Error while deleting worklog"});
  },

};

export default worklogApi;