import axios from "./config/axiosConfig.js";

const logEntryApi = {
  key: "logEntries",

  listAll: async () => {
    const {data} = await axios.get("/logEntries", {baseMsg: "Error while fetching log entries"});
    return data.items;
  },

  create: async ({ticket, startTime, description}) => {
    const {data} = await axios.post("/logEntries", {ticket, startTime, description}, {baseMsg: "Error while creating log entry"});
    return data;
  },

  update: async ({id, ticket, startTime, endTime, description}) => {
    const {data} = await axios.put(`/logEntries/${id}`, {
      ticket,
      startTime,
      endTime,
      description
    }, {baseMsg: "Error while updating log entry"});
    return data;
  },

  delete: async (id) => {
    await axios.delete(`/logEntries/${id}`, {baseMsg: "Error while deleting log entry"});
  }
};

export default logEntryApi;