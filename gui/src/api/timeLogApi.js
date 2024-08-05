import axios from "./config/axiosConfig.js";

const key = "time-logs"
const timeLogApi = {
  key,
  list: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}`, {params, baseMsg: "Error while fetching time logs"});
    return data.items;
  },

  create: async (body) => {
    const {data} = await axios.post(`/${key}`, {...body}, {baseMsg: "Error while creating time log"});
    return data;
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
  }
};

export default timeLogApi;