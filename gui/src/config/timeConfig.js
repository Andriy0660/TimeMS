import timeLogApi from "../api/timeLogApi.js";

let startHourOfDay = 0;
let startHourOfWorkingDay = 9;
let endHourOfWorkingDay = 18;

const initializeOffset = async () => {
  const res = await timeLogApi.getConfig();
  startHourOfDay = res.offset;
  startHourOfWorkingDay = res.startHourOfWorkingDay;
  endHourOfWorkingDay = res.endHourOfWorkingDay;
};

initializeOffset();
export {startHourOfDay, startHourOfWorkingDay, endHourOfWorkingDay};