import timeLogApi from "../api/timeLogApi.js";

let startHourOfDay = 0;
let startHourOfWorkingDay = 9;
let endHourOfWorkingDay = 18;
let isJiraSyncingEnabled = false;
let isExternalServiceSyncingEnabled = false;
let externalTimeLogTimeCf = 1;
let externalServiceIncludeDescription = false;

const initialize = async () => {
  const res = await timeLogApi.getConfig();
  startHourOfDay = res.offset;
  startHourOfWorkingDay = res.startHourOfWorkingDay;
  endHourOfWorkingDay = res.endHourOfWorkingDay;
  isJiraSyncingEnabled = res.isJiraSyncingEnabled;
  isExternalServiceSyncingEnabled = res.isExternalServiceSyncingEnabled;
  externalTimeLogTimeCf = res.externalTimeLogTimeCf;
  externalServiceIncludeDescription = res.externalServiceIncludeDescription
};

let googleClientId = "767173580299-fcnbh5h922avn99lktktjb9uq9f5itcc.apps.googleusercontent.com";

initialize();
export {
  startHourOfDay,
  startHourOfWorkingDay,
  endHourOfWorkingDay,
  isJiraSyncingEnabled,
  isExternalServiceSyncingEnabled,
  externalTimeLogTimeCf,
  externalServiceIncludeDescription,
  googleClientId
};