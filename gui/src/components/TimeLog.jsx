import {Chip, IconButton, LinearProgress, TextField, Tooltip, Typography} from "@mui/material";
import {TimeField} from "@mui/x-date-pickers";
import dayjs from "dayjs";
import {useEffect, useMemo, useRef, useState} from "react";
import BackspaceOutlinedIcon from '@mui/icons-material/BackspaceOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import KeyboardTabOutlinedIcon from '@mui/icons-material/KeyboardTabOutlined';
import StopCircleOutlinedIcon from '@mui/icons-material/StopCircleOutlined';
import StartOutlinedIcon from '@mui/icons-material/StartOutlined';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import Divider from "@mui/material/Divider";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../utils/dateTimeService.js";

export default function TimeLog({
  timeLog,
  onCreate,
  onUpdate,
  onDelete
}) {
  const currentTime = dayjs();
  const [ticket, setTicket] = useState(timeLog.ticket || "");
  const [startTime, setStartTime] = useState(timeLog.startTime ? dayjs(timeLog.startTime) : null);
  const [endTime, setEndTime] = useState(timeLog.endTime ? dayjs(timeLog.endTime) : null);
  const [description, setDescription] = useState(timeLog.description || "");
  const [totalTime, setTotalTime] = useState(timeLog.totalTime);

  const status = useMemo(() => {
    if (totalTime) {
      return "Done";
    } else if (startTime) {
      return "InProgress";
    } else return "Pending";
  }, [totalTime, startTime]);

  const [isLoading, setIsLoading] = useState(false);

  const [isEditing, setIsEditing] = useState(false);
  const [editedField, setEditedField] = useState(null);
  const [isHovered, setIsHovered] = useState(false);

  const timeLogRef = useRef(null);
  const {addAlert} = useAppContext();

  const initializeState = () => {
    setTicket(timeLog?.ticket || "");
    setStartTime(timeLog?.startTime ? dayjs(timeLog.startTime) : null);
    setEndTime(timeLog?.endTime ? dayjs(timeLog.endTime) : null);
    setDescription(timeLog?.description || "");
    setTotalTime(timeLog?.totalTime || "");
  };

  useEffect(() => {
    initializeState();
  }, [timeLog]);

  const resetChanges = () => {
    initializeState();
    setIsEditing(false);
  };

  const handleUpdateTimeLog = async (body) => {
    setIsLoading(true);
    setIsEditing(false);
    try {
      await onUpdate(body);
    } catch (error) {
      resetChanges();
    } finally {
      setIsLoading(false);
    }
  };
  const isSameDate = (date1, date2) => {
    if (!date1 && !date2) return true;
    if (!date1 || !date2) return false;
    return date1.isSame(dayjs(date2));
  };

  const isModified = useMemo(() => {
    return (
      ticket !== timeLog.ticket ||
      description !== timeLog.description ||
      !isSameDate(startTime, timeLog.startTime) ||
      !isSameDate(endTime, timeLog.endTime)
    );
  }, [ticket, description, startTime, endTime, timeLog]);

  const isTimeFieldsValid = (startTime && endTime) ? startTime?.isBefore(endTime) : true;

  const jiraIssuePattern = /^[A-Z]{2,}-\d+/;
  const isTicketFieldValid = ticket ? ticket?.match(jiraIssuePattern) : true;
  const validateUpdateRequest = () => {
    if (!isTimeFieldsValid) {
      addAlert({
        text: "End time must be great than start time",
        type: "error"
      })
      setEndTime(timeLog.endTime ? dayjs(timeLog.endTime) : null);
      setStartTime(timeLog.startTime ? dayjs(timeLog.startTime) : null);
      return false;
    }

    if (!isTicketFieldValid) {
      addAlert({
        text: "Invalid ticket number",
        type: "error"
      })
      setTicket(timeLog?.ticket || "");
      return false;
    }
    return true;
  }
  const handleClickOutside = (event) => {
      if (timeLogRef.current && !timeLogRef.current.contains(event.target)) {
        setIsEditing(false);
        if (isModified && validateUpdateRequest()) {
          handleUpdateTimeLog({
            id: timeLog.id,
            ticket,
            startTime: dateTimeService.getFormattedDateTime(startTime),
            endTime: dateTimeService.getFormattedDateTime(endTime),
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
  }, [timeLogRef.current, startTime, endTime, ticket, description]);

  useEffect(() => {
    if (isEditing && editedField) {
      timeLogRef.current.querySelector(`[name="${editedField}"]`).focus();
    }
  }, [isEditing, editedField]);

  const statusConfig = {
    Done: {
      label: totalTime,
      action: isHovered && (
        <Tooltip title="continue">
          <IconButton
            onClick={async () => {
              setIsLoading(true);
              try {
                await onCreate({ticket, startTime: dateTimeService.getFormattedDateTime(currentTime), description});
              } finally {
                setIsLoading(false);
              }
            }}
            variant="outlined"
            color="primary"
            className="mr-2"
          >
            <KeyboardTabOutlinedIcon />
          </IconButton>
        </Tooltip>
      ),
    },
    InProgress: {
      label: currentTime.diff(timeLog?.startTime) >= 0
        ? `${currentTime.diff(timeLog?.startTime, "hour")}h ${currentTime.diff(timeLog?.startTime, "minute") % 60}m`
        : null,
      action: isHovered && (
        <Tooltip title="stop">
          <IconButton
            onClick={() => {
              setIsLoading(true);
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime: dateTimeService.getFormattedDateTime(startTime),
                endTime: dateTimeService.getFormattedDateTime(currentTime),
                description,
              });
              setIsLoading(false);
            }}
            variant="outlined"
            color="warning"
            className="mr-2"
          >
            <StopCircleOutlinedIcon />
          </IconButton>
        </Tooltip>
      ),
    },
    Pending: {
      label: 'Pending',
      action: isHovered && (
        <Tooltip title="start">
          <IconButton
            onClick={() => {
              setIsLoading(true);
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime: dateTimeService.getFormattedDateTime(currentTime),
                description,
              });
              setIsLoading(false);
            }}
            variant="outlined"
            color="primary"
            className="mr-2"
          >
            <StartOutlinedIcon />
          </IconButton>
        </Tooltip>
      ),
    },
  };

  function getEditableFields() {
    return <>
      <div className="mr-4 my-2">
        <TextField
          error={!isTicketFieldValid}
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
          error={!isTimeFieldsValid}
          className="w-20"
          label="Start"
          size="small"
          value={startTime}
          onChange={(date) => {
            if (date === null) {
              setStartTime(null);
            } else if (dayjs(date).isValid()) {
              setStartTime(dayjs(date))
            }
          }}
          format="HH:mm"
        />
      </div>

      <div className="mr-4 my-2">
        <TimeField
          name="endTime"
          error={!isTimeFieldsValid}
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
    </>;
  }

  function getNonEditableFields() {
    return <>
      {startTime &&
        <div
          className="mr-4 my-2 hover:bg-blue-100"
          onClick={() => {
            setIsEditing(true);
            setEditedField("startTime");
          }}
        >
          <Typography className="font-bold">{dateTimeService.getFormattedTime(startTime)}</Typography>
        </div>
      }
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
            <Typography className="font-bold">{dateTimeService.getFormattedTime(endTime)}</Typography>
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

    </>;
  }

  return (
    <div
      className={`p-4 ${status === "InProgress" ? "bg-blue-50" : ""}`}
      ref={timeLogRef}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="flex items-center">
          {isEditing ? getEditableFields() : getNonEditableFields()}
          {statusConfig[status].label ? <Chip
            label={statusConfig[status].label}
            color="primary"
            variant="outlined"
            size="small"
            className="shadow-md mr-2"
          /> : null}

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
                      onClick={() => handleUpdateTimeLog({
                        id: timeLog.id,
                        ticket,
                        startTime: dateTimeService.getFormattedDateTime(startTime),
                        endTime: dateTimeService.getFormattedDateTime(endTime),
                        description
                      })}
                      className="mr-0"
                      color="success"
                      disabled={!isTimeFieldsValid || !isTicketFieldValid}
                    >
                      <SaveOutlinedIcon fontSize="small" />
                    </IconButton>
                  </span>
                </Tooltip>
              </div>
            )}

            {statusConfig[status] ? statusConfig[status].action : null}

            {(isHovered && !isEditing) && (
              <>
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
                <Tooltip title="Delete">
                  <IconButton
                    color="error"
                    onClick={() => {
                      setIsLoading(true);
                      onDelete(timeLog.id);
                      setIsLoading(false);
                    }}
                  >
                    <DeleteOutlineOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </>
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
      {isLoading && <LinearProgress /> }
    </div>
  );
}