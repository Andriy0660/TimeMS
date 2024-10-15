import CustomTimeField from "./CustomTimeField.jsx";
import {TextField} from "@mui/material";
import dateTimeService from "../../service/dateTimeService.js";
import {isJiraSyncingEnabled} from "../../config/config.js";

export default function TimeLogEditableFields({
  startTime,
  setStartTime,
  startTimeError,
  setStartTimeError,
  endTime,
  setEndTime,
  endTimeError,
  setEndTimeError,
  ticket,
  setTicket,
  isTicketFieldValid,
  timeLog,
}) {

  return (
    <div className="flex items-center">
      <CustomTimeField
        className="mr-4"
        name="startTime"
        label="Start"
        value={startTime}
        setValue={setStartTime}
        error={startTimeError}
        setError={setStartTimeError}
        getNewValue={(timeToSet) =>
          dateTimeService.buildStartTime(timeLog.date, timeToSet)
        }
      />
      <CustomTimeField
        className="mr-4"
        name="endTime"
        label="End"
        value={endTime}
        setValue={setEndTime}
        error={endTimeError}
        setError={setEndTimeError}
        getNewValue={(timeToSet) =>
          dateTimeService.buildEndTime(timeLog.date, startTime, timeToSet)
        }
      />

      {isJiraSyncingEnabled && (
        <TextField
          error={!isTicketFieldValid}
          name="ticket"
          className="w-24 mr-4"
          label="Ticket"
          size="small"
          value={ticket}
          onChange={(event) => setTicket(event.target.value)}
          autoComplete="off"
          inputProps={{
            style: {textTransform: "uppercase"}
          }}
        />
      )}
    </div>
  );
};