import CustomTimeField from "./CustomTimeField.jsx";
import {TextField} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";

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
  const {isJiraSyncingEnabled} = useAppContext();

  return (
    <div className="flex items-center">
      <CustomTimeField
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
        <div className="mr-4">
          <TextField
            error={!isTicketFieldValid}
            name="ticket"
            className="w-24"
            label="Ticket"
            size="small"
            value={ticket}
            onChange={(event) => setTicket(event.target.value)}
            autoComplete="off"
            inputProps={{
              style: {textTransform: "uppercase"}
            }}
          />
        </div>
      )}
    </div>
  );
};