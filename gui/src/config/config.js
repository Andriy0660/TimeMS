import configApi from "../api/configApi.js";

let startHourOfDay = 0;
let startHourOfWorkingDay = 9;
let endHourOfWorkingDay = 18;
let isJiraSyncingEnabled = false;
let isExternalServiceSyncingEnabled = false;
let externalServiceTimeCf = 1;
let externalServiceIncludeDescription = true;

const initializeConfig = async () => {
  const res = await configApi.getConfig();
  startHourOfDay = res?.dayOffsetHour;
  startHourOfWorkingDay = res?.workingDayStartHour;
  endHourOfWorkingDay = res?.workingDayEndHour;
  isJiraSyncingEnabled = res?.isJiraEnabled;
  isExternalServiceSyncingEnabled = res?.isExternalServiceEnabled;
  externalServiceTimeCf = res?.externalServiceTimeCf;
  externalServiceIncludeDescription = res?.isExternalServiceIncludeDescription
};

let googleClientId = "767173580299-fcnbh5h922avn99lktktjb9uq9f5itcc.apps.googleusercontent.com";

initializeConfig();
export {
  initializeConfig,
  startHourOfDay,
  startHourOfWorkingDay,
  endHourOfWorkingDay,
  isJiraSyncingEnabled,
  isExternalServiceSyncingEnabled,
  externalServiceTimeCf,
  externalServiceIncludeDescription,
  googleClientId
};