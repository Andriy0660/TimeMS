import axios from "./config/axiosConfig.js";

const syncExternalServiceApi = {
  syncIntoExternalService: async (body) => {
    const {data} = await axios.post(`/syncExternalService/to`, {...body}, {baseMsg: "Error while synchronizing into external service"});
    return data;
  },
};

export default syncExternalServiceApi;