import axios from "./config/axiosConfig.js";

const key = "time-logs"
const timeLogApi = {
  key,
  list: async (body) => {
    const params = new URLSearchParams({...body});
    const {data} = await axios.get(`/${key}`, {params, baseMsg: "Error while fetching time logs"});
    return data.items;
  },

  create: async ({ticket, startTime, description}) => {
    const {data} = await axios.post(`/${key}`, {ticket, startTime, description}, {baseMsg: "Error while creating time log"});
    return data;
  },

  update: async ({id, ticket, startTime, endTime, description}) => {
    const {data} = await axios.put(`/${key}/${id}`, {
      ticket,
      startTime,
      endTime,
      description
    }, {baseMsg: "Error while updating time log"});
    return data;
  },

  delete: async (id) => {
    await axios.delete(`/${key}/${id}`, {baseMsg: "Error while deleting time log"});
  }
};

export default timeLogApi;