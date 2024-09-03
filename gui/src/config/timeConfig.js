import timeLogApi from "../api/timeLogApi.js";

let startHourOfDay = 0;

const initializeOffset = async () => {
  startHourOfDay = await timeLogApi.getOffset();
};

initializeOffset();

const startHourOfWorkingDay = 7;
const endHourOfWorkingDay = 17;
export {startHourOfDay, startHourOfWorkingDay, endHourOfWorkingDay};