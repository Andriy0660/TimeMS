import timeLogApi from "../api/timeLogApi.js";

const startHourOfDay = await timeLogApi.getOffset()

const startHourOfWorkingDay = 7;
const endHourOfWorkingDay = 17;
export {startHourOfDay, startHourOfWorkingDay, endHourOfWorkingDay};