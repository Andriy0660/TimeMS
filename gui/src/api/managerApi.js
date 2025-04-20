// gui/src/api/managerApi.js
import axios from "./config/axiosConfig.js";

const key = "manager"
const managerApi = {
  key,

  getAllUsers: async () => {
    const {data} = await axios.get(`/${key}/users`, {
      baseMsg: "Error while fetching users"
    });
    return data;
  },

  getUserInfo: async (userId) => {
    const {data} = await axios.get(`/${key}/users/${userId}`, {
      baseMsg: "Error while fetching user info"
    });
    return data;
  },

  getAllTenants: async () => {
    const {data} = await axios.get(`/${key}/tenants`, {
      baseMsg: "Error while fetching tenants"
    });
    return data;
  },

  getTenantInfo: async (tenantId) => {
    const {data} = await axios.get(`/${key}/tenants/${tenantId}`, {
      baseMsg: "Error while fetching tenant info"
    });
    return data;
  }
};

export default managerApi;