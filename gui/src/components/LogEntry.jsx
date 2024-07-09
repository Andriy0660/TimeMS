import {Button, Grow, IconButton, TextField, Tooltip} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useEffect, useState} from "react";
import BackspaceOutlinedIcon from '@mui/icons-material/BackspaceOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';

export default function LogEntry({logEntry}) {
  const [ticket, setTicket] = useState(logEntry.ticket || "");
  const [startTime, setStartTime] = useState(dayjs(logEntry.startTime, "hh:mm"));
  const [endTime, setEndTime] = useState(logEntry.endTime ? dayjs(logEntry.endTime, "hh:mm") : null);
  const [description, setDescription] = useState(logEntry.description || "");

  const [showTimeFields, setShowTimeFields] = useState(true);

  const resetChanges = () => {
    console.log("reset")
    setTicket(logEntry.ticket || "");
    setStartTime(dayjs(logEntry.startTime));
    setEndTime(logEntry.endTime ? dayjs(logEntry.endTime) : null);
    setDescription(logEntry.description || "")
    setShowTimeFields(true)
    localStorage.removeItem(`LogEntry_${logEntry.id}`)
  }

  const handleSaveLogEntry = () => {
    console.log("saved")
    setShowTimeFields(true)
    localStorage.removeItem(`LogEntry_${logEntry.id}`)
  }

  const isBodyModified = ticket !== logEntry.ticket || description !== logEntry.description;

  const isTimeModified = (
    !startTime.isSame(dayjs(logEntry.startTime, "HH:mm")) ||
    (endTime !== null && !endTime.isSame(dayjs(logEntry.endTime, "HH:mm")))
  );

  useEffect(() => {
    const storedLogEntry = JSON.parse(localStorage.getItem(`LogEntry_${logEntry.id}`));
    if (storedLogEntry) {
      setTicket(storedLogEntry.ticket || "");
      setDescription(storedLogEntry.description || "");
    }
  }, [logEntry.id]);

  const addToLocalStorage = () => {
    const savedLogEntry = {
      ticket,
      description,
    };
    localStorage.setItem(`LogEntry_${logEntry.id}`, JSON.stringify(savedLogEntry));
    console.log("Saved to localStorage");
  };

  return (
    <div
      className="flex items-center"
      onKeyDown={async (e) => {
        if (e.key === "Enter") {
          handleSaveLogEntry();
        }
      }}
      onBlur={(e) => {
        if (isBodyModified) {
          addToLocalStorage();
        }
        if (isTimeModified) {
          handleSaveLogEntry();
        }
      }}
    >
      <div className="mx-2 my-2">
        <TextField
          className="w-24"
          label="Ticket"
          size="small"
          value={ticket}
          onChange={(event) => setTicket(event.target.value)}
          autoComplete="off"
        />
      </div>
      {showTimeFields ? (
        <>
          <Grow timeout={500} in={showTimeFields}>
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
          </Grow>
          <Grow timeout={500} in={showTimeFields}>
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
          </Grow>
        </>
      ) : null}

      <div className="min-w-40 w-full mx-2 my-2">
        <TextField
          className="w-full"
          label="Description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          size="small"
          autoComplete="off"
          onClick={() => setShowTimeFields(false)}
          onBlur={(e) => {
            if (!e.relatedTarget) {
              setShowTimeFields(true)
            }
          }}
          onKeyDown={async (e) => {
            if (e.key === "Enter") {
              setShowTimeFields(true);
            }
          }}
        />
      </div>
      <div className="mx-0 my-2 flex">
        {isBodyModified ? (<div className="flex">
          <Tooltip onClick={() => resetChanges()} title="Reset">
            <IconButton className="mr-1">
              <BackspaceOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>

          <Tooltip title="Save">
            <IconButton onClick={() => handleSaveLogEntry()} className="mr-1" color="success">
              <SaveOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>

        </div>) : null}

        {logEntry.totalTime ?
          <Button variant="outlined">Continue</Button> :
          <Button color="warning" variant="outlined">Stop</Button>
        }
      </div>

      <div className="mx-2 my-2 text-sm whitespace-nowrap">
        {logEntry.totalTime ?? "In Progress..."}
      </div>

    </div>

  );
}