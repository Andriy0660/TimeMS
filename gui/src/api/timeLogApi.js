import axios from "./config/axiosConfig.js";

const key = "time-logs"
const timeLogApi = {
  key,
  list: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}`, {params, baseMsg: "Error while fetching time logs"});
    return data.items;
  },

  getHoursForWeek: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}/hoursForWeek`, {params, baseMsg: "Error while getting hours for week"});
    return data.items;
  },

  getHoursForMonth: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}/hoursForMonth`, {params, baseMsg: "Error while getting hours for month"});
    return data;
  },

  create: async (body) => {
    const {data} = await axios.post(`/${key}`, {...body}, {baseMsg: "Error while creating time log"});
    return data;
  },

  createFromWorklog: async (body) => {
    const {data} = await axios.post(`/${key}/fromWorklog`, {...body}, {baseMsg: "Error while creating time log from worklog"});
    return data;
  },

  divide: async (id) => {
    await axios.post(`/${key}/divide/${id}`, {}, {baseMsg: "Error while dividing time logs"});
  },

  importTimeLogs: async (body) => {
    await axios.post(`/${key}/importTimeLogs`, {...body}, {baseMsg: "Error while importing time logs"});
  },

  update: async ({id, ...body}) => {
    const {data} = await axios.put(`/${key}/${id}`, {...body}, {baseMsg: "Error while updating time log"});
    return data;
  },

  delete: async (id) => {
    await axios.delete(`/${key}/${id}`, {baseMsg: "Error while deleting time log"});
  },

  setGroupDescription: async (body) => {
    await axios.patch(`/${key}/setGroupDescription`, {...body}, {baseMsg: "Error while setting description"});
  },

  changeDate: async ({id, ...body}) => {
    await axios.patch(`/${key}/${id}/changeDate`, {...body}, {baseMsg: "Error while changing date"});
  }
};

export default timeLogApi;