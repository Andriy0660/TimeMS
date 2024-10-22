import timeLogApi from "../api/timeLogApi.js";

let startHourOfDay = 0;
let startHourOfWorkingDay = 9;
let endHourOfWorkingDay = 18;
let isJiraSyncingEnabled = false;

const initialize = async () => {
  const res = await timeLogApi.getConfig();
  startHourOfDay = res.offset;
  startHourOfWorkingDay = res.startHourOfWorkingDay;
  endHourOfWorkingDay = res.endHourOfWorkingDay;
  isJiraSyncingEnabled = res.isJiraSyncingEnabled;
};

initialize();
export {startHourOfDay, startHourOfWorkingDay, endHourOfWorkingDay, isJiraSyncingEnabled};