import {Chip, IconButton, LinearProgress, TextField, Tooltip, Typography} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useEffect, useRef, useState} from "react";
import BackspaceOutlinedIcon from '@mui/icons-material/BackspaceOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import StopCircleOutlinedIcon from '@mui/icons-material/StopCircleOutlined';
import StartOutlinedIcon from '@mui/icons-material/StartOutlined';
import Divider from "@mui/material/Divider";
import useAppContext from "../context/useAppContext.js";

export default function LogEntry({
  logEntry,
  onCreate,
  onUpdate
}) {
  const [ticket, setTicket] = useState(logEntry.ticket || "");
  const [startTime, setStartTime] = useState(dayjs(logEntry.startTime));
  const [endTime, setEndTime] = useState(logEntry.endTime ? dayjs(logEntry.endTime) : null);
  const [description, setDescription] = useState(logEntry.description || "");
  const [totalTime, setTotalTime] = useState(logEntry.totalTime || "In Progress");

  const [isCreating, setIsCreating] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isStopping, setIsStopping] = useState(false);

  const [isEditing, setIsEditing] = useState(false);
  const [editedField, setEditedField] = useState(null);
  const [isHovered, setIsHovered] = useState(false);

  const logEntryRef = useRef(null);
  const {addAlert} = useAppContext();

  useEffect(() => {
    setTicket(logEntry?.ticket)
    setStartTime(dayjs(logEntry.startTime))
    setEndTime(logEntry.endTime ? dayjs(logEntry.endTime) : null)
    setDescription(logEntry?.description)
    setTotalTime(logEntry.totalTime || "In Progress")
  }, [logEntry])

  const resetChanges = () => {
    console.log("reset");
    setTicket(logEntry.ticket || "");
    setStartTime(dayjs(logEntry.startTime));
    setEndTime(logEntry.endTime ? dayjs(logEntry.endTime) : null);
    setDescription(logEntry.description || "");
    setIsEditing(false);
  };

  const handleUpdateLogEntry = async (body) => {
    setIsUpdating(true);
    setIsEditing(false);
    try {
      await onUpdate(body);
    } catch (error) {
      resetChanges();
    } finally {
      setIsUpdating(false);
    }
  };

  const isModified = (
    ticket !== logEntry.ticket ||
    description !== logEntry.description ||
    !startTime.isSame(dayjs(logEntry.startTime)) ||
    ((endTime || logEntry.endTime) && !endTime?.isSame(dayjs(logEntry?.endTime)))
  );

  const handleClickOutside = (event) => {
    if (logEntryRef.current && !logEntryRef.current.contains(event.target)) {
      setIsEditing(false);
      if (isModified) {
        if (startTime.isAfter(endTime)) {
          addAlert({
            text: "End time must be great than start time",
            type: "error"
          })
          setEndTime(logEntry.endTime ? dayjs(logEntry.endTime) : null);
          return;
        }
        handleUpdateLogEntry({
          id: logEntry.id,
          ticket,
          startTime: startTime.format("YYYY-MM-DDTHH:mm"),
          endTime: endTime?.format("YYYY-MM-DDTHH:mm"),
          description
        });
      }
    }
  };

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [handleClickOutside]);

  useEffect(() => {
    if (isEditing && editedField) {
      logEntryRef.current.querySelector(`[name="${editedField}"]`).focus();
    }
  }, [isEditing, editedField]);

  return (
    <div
      className={`p-4 ${totalTime==="In Progress"?"bg-blue-50":""}`}
      ref={logEntryRef}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="flex items-center">
          {isEditing ? (
            <>
              <div className="mr-4 my-2">
                <TextField
                  name="ticket"
                  className="w-24"
                  label="Ticket"
                  size="small"
                  value={ticket}
                  onChange={(event) => setTicket(event.target.value)}
                  autoComplete="off"
                />
              </div>

              <div className="mr-4 my-2">
                <TimeField
                  name="startTime"
                  className="w-20"
                  label="Start"
                  size="small"
                  value={startTime}
                  onChange={(date) => {
                    if (dayjs(date).isValid() && date !== null) {
                      setStartTime(dayjs(date))
                    }
                  }}
                  format="HH:mm"
                />
              </div>

              <div className="mr-4 my-2">
                <TimeField
                  name="endTime"
                  className="w-20"
                  label="End"
                  value={endTime}
                  onChange={(date) => {
                    if (date === null) {
                      setEndTime(null);
                    } else if (dayjs(date).isValid()) {
                      setEndTime(dayjs(date))
                    }
                  }}
                  size="small"
                  format="HH:mm"
                />
              </div>
            </>
          ) : (
            <>
              <div
                className="mr-4 my-2 hover:bg-blue-100"
                onClick={() => {
                  setIsEditing(true);
                  setEditedField("startTime");
                }}
              >
                <Typography className="font-bold">{startTime.format("HH:mm")}</Typography>
              </div>
              {endTime && (
                <>
                  -
                  <div
                    className="mx-4 my-2 hover:bg-blue-100"
                    onClick={() => {
                      setIsEditing(true);
                      setEditedField("endTime");
                    }}
                  >
                    <Typography className="font-bold">{endTime?.format("HH:mm")}</Typography>
                  </div>
                </>
              )}

              {ticket && (
                <>
                  <Divider className="bg-gray-500 mr-4" orientation="vertical" variant="middle" sx={{borderRightWidth: 2}} flexItem />
                  <div
                    className="mr-4 my-2 hover:bg-blue-100"
                    onClick={() => {
                      setIsEditing(true);
                      setEditedField("ticket");
                    }}
                  >
                    <Typography className="font-bold">{ticket}</Typography>
                  </div>
                </>
              )}

            </>
          )}
          <Chip
            label={totalTime}
            color="primary"
            variant="outlined"
            size="small"
            className="shadow-md"
          />
          {(totalTime==="In Progress" && dayjs().isAfter(startTime)) ? (
            <Chip
              label={`${dayjs().diff(startTime, "hour")}h ${dayjs().diff(startTime, "minute")}m`}
              color="primary"
              variant="outlined"
              size="small"
              className="shadow-md mx-2"
            />
          ):null}
        </div>

        <div className="flex items-center">
          <div className="flex ">
            {(isEditing) && (
              <div>
                <Tooltip onClick={() => resetChanges()} title="Reset">
                  <IconButton className="mr-0">
                    <BackspaceOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>

                <Tooltip title="Save">
                  <span>
                    <IconButton
                      onClick={() => handleUpdateLogEntry({
                        id: logEntry.id,
                        ticket,
                        startTime: startTime.format("YYYY-MM-DDTHH:mm"),
                        endTime: endTime?.format("YYYY-MM-DDTHH:mm"),
                        description
                      })}
                      className="mr-0"
                      color="success"
                      disabled={startTime.isAfter(endTime)}
                    >
                      <SaveOutlinedIcon fontSize="small" />
                    </IconButton>
                  </span>
                </Tooltip>
              </div>
            )}
            {(isHovered && !isEditing) && (
              <Tooltip title="Edit">
                <IconButton
                  className="mr-2"
                  color="success"
                  onMouseDown={() => {
                    setTimeout(() => {
                      setIsEditing(true);
                    }, 0)
                  }}
                >
                  <EditOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            )}
            {(!isEditing && isHovered) && (logEntry.endTime ?
                <Tooltip title="continue">
                  <IconButton
                    onClick={async () => {
                      setIsCreating(true);
                      try {
                        await onCreate({ticket, startTime: dayjs().format("YYYY-MM-DDTHH:mm"), description});
                      } finally {
                        setIsCreating(false);
                      }
                    }}
                    variant="outlined"
                    color="primary">
                    <StartOutlinedIcon />
                  </IconButton>
                </Tooltip>
                :
                <Tooltip title="stop">
                  <IconButton
                    onClick={() => {
                      setIsStopping(true);
                      setEndTime(dayjs());
                      handleUpdateLogEntry({
                        id: logEntry.id,
                        ticket,
                        startTime: startTime.format("YYYY-MM-DDTHH:mm"),
                        endTime: dayjs().format("YYYY-MM-DDTHH:mm"),
                        description
                      });
                      setIsStopping(false);
                    }}
                    variant="outlined"
                    color="warning">
                    <StopCircleOutlinedIcon />
                  </IconButton>
                </Tooltip>
            )}
          </div>
        </div>
      </div>

      <div
        className={`mt-1 ${isEditing ? "" : "hover:bg-blue-100"}`}
        onClick={() => {
          setIsEditing(true);
          setEditedField("description");
        }}
      >
        {isEditing ? (
          <TextField
            name="description"
            className="w-full"
            label="Description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            size="small"
            autoComplete="off"
            multiline
            onFocus={(e) => {
              const value = e.target.value
              e.target.setSelectionRange(value.length, value.length);
            }}
          />
        ) : (
          <div className="text-justify whitespace-pre-wrap">{description}</div>
        )}
      </div>
      {(isCreating || isUpdating || isStopping) &&
        <LinearProgress />
      }
    </div>
  );
}