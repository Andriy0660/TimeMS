import axios from "./config/axiosConfig.js";

const syncUpworkApi = {
  getStatus: async (date) => {
    const params = new URLSearchParams({date});
    const {data} = await axios.get(`/syncUpwork`, {params, baseMsg: "Error while getting upwork status for day"});
    return data.upworkSyncInfo.status;
  },

  sync: async (body) => {
    await axios.post(`/syncUpwork`, {...body}, {baseMsg: "Error while synchronizing upwork"});
  },

};

export default syncUpworkApi;