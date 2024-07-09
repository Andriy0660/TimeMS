import {Button, IconButton, TextField, Tooltip} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useState} from "react";
import BackspaceOutlinedIcon from '@mui/icons-material/BackspaceOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';

export default function LogEntry({logEntry}) {
  const [ticket, setTicket] = useState(logEntry.ticket || "");
  const [startTime, setStartTime] = useState(dayjs(logEntry.startTime, "hh:mm"));
  const [endTime, setEndTime] = useState(logEntry.endTime ? dayjs(logEntry.endTime, "hh:mm") : null);
  const [description, setDescription] = useState(logEntry.description || "");

  const resetChanges = () => {
    console.log("reset")
    setTicket(logEntry.ticket || "");
    setStartTime(dayjs(logEntry.startTime));
    setEndTime(logEntry.endTime ? dayjs(logEntry.endTime) : null);
    setDescription(logEntry.description || "")
  }

  const handleSaveLogEntry = () => {
    console.log("saved")
  }

  const isBodyModified = ticket !== logEntry.ticket || description !== logEntry.description;

  const isTimeModified = (
    !startTime.isSame(dayjs(logEntry.startTime, "HH:mm")) ||
    (endTime !== null && !endTime.isSame(dayjs(logEntry.endTime, "HH:mm")))
  );

  return (
    <div className="hover:bg-gray-200 p-2">
      <div className="flex justify-between">
        <div
          className="flex items-center"
          onKeyDown={async (e) => {
            if (e.key === "Enter") {
              handleSaveLogEntry();
            }
          }}
          onBlur={(e) => {

            if (isTimeModified) {
              handleSaveLogEntry();
            }
          }}
        >
          <div className="mr-2 my-2">
            <TextField
              className="w-24"
              label="Ticket"
              size="small"
              value={ticket}
              onChange={(event) => setTicket(event.target.value)}
              autoComplete="off"
            />
          </div>

          <div className="mx-2 my-2">
            <TimeField
              className="w-20"
              label="Start"
              size="small"
              value={startTime}
              onChange={(date) =>
                setStartTime(dayjs(date))}
              format="HH:mm"
            />
          </div>

          <div className="mx-2 my-2">
            <TimeField
              className="w-20"
              label="End"
              value={endTime}
              onChange={(date) =>
                setEndTime(date ? dayjs(date) : null)}
              size="small"
              format="HH:mm"
            />
          </div>


          <div className="mx-2 my-2 text-sm whitespace-nowrap">
            {logEntry.totalTime ?? "In Progress..."}
          </div>
        </div>
        <div className="flex items-center">
          <div className="flex ">
            {isBodyModified && (
              <div>
                <Tooltip onClick={() => resetChanges()} title="Reset">
                  <IconButton className="mr-0">
                    <BackspaceOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>

                <Tooltip title="Save">
                  <IconButton onClick={() => handleSaveLogEntry()} className="mr-0" color="success">
                    <SaveOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>

              </div>)
            }

            {logEntry.totalTime ?
              <Button variant="outlined">Continue</Button> :
              <Button color="warning" variant="outlined">Stop</Button>
            }
          </div>
        </div>
      </div>

      <div>
        <TextField
          className="w-full"
          label="Description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          size="small"
          autoComplete="off"
          multiline
        />
      </div>
    </div>
  );
}