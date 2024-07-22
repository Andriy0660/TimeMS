import axios from "./config/axiosConfig.js";

const timeLogApi = {
  key: "logEntries",

  listAll: async () => {
    const {data} = await axios.get("/logEntries", {baseMsg: "Error while fetching time logs"});
    return data.items;
  },

  create: async ({ticket, startTime, description}) => {
    const {data} = await axios.post("/logEntries", {ticket, startTime, description}, {baseMsg: "Error while creating time log"});
    return data;
  },

  update: async ({id, ticket, startTime, endTime, description}) => {
    const {data} = await axios.put(`/logEntries/${id}`, {
      ticket,
      startTime,
      endTime,
      description
    }, {baseMsg: "Error while updating time log"});
    return data;
  },

  delete: async (id) => {
    await axios.delete(`/logEntries/${id}`, {baseMsg: "Error while deleting time log"});
  }
};

export default timeLogApi;