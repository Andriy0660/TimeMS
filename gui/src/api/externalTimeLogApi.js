import axios from "./config/axiosConfig.js";

const key = "external-time-logs"
const externalTimeLogApi = {
  key,
  list: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}`, {params, baseMsg: "Error while fetching external time logs"});
    return data.items;
  },

  create: async (body) => {
    const {data} = await axios.post(`/${key}`, {...body}, {baseMsg: "Error while creating external time log"});
    return data;
  },

  delete: async (id) => {
    await axios.delete(`/${key}/${id}`, {baseMsg: "Error while deleting external time log"});
  },

};
export default externalTimeLogApi;