// gui/src/api/adminApi.js
import axios from "./config/axiosConfig.js";

const key = "admin"
const adminApi = {
  key,

  getAuditLogs: async (tenantId, page = 0, size = 10) => {
    const {data} = await axios.get(`/${key}/audit/tenant/${tenantId}?page=${page}&size=${size}`, {
      baseMsg: "Error while fetching audit logs"
    });
    return data;
  },

  getUserAuditLogs: async (userId, page = 0, size = 10) => {
    const {data} = await axios.get(`/${key}/audit/user/${userId}?page=${page}&size=${size}`, {
      baseMsg: "Error while fetching user audit logs"
    });
    return data;
  },

  toggleUserActive: async (userId) => {
    await axios.post(`/${key}/users/${userId}/toggle-active`, {}, {
      baseMsg: "Error toggling user active status"
    });
  },

  getAllRoles: async () => {
    const {data} = await axios.get(`/${key}/roles`, {
      baseMsg: "Error fetching roles"
    });
    return data;
  },

  addRoleToUser: async (userId, roleName) => {
    await axios.post(`/${key}/roles/user/${userId}/add?roleName=${roleName}`, {}, {
      baseMsg: "Error adding role to user"
    });
  },

  removeRoleFromUser: async (userId, roleName) => {
    await axios.post(`/${key}/roles/user/${userId}/remove?roleName=${roleName}`, {}, {
      baseMsg: "Error removing role from user"
    });
  }
};

export default adminApi;