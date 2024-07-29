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
import ConfirmationModal from "./ConfirmationModal.jsx";

export default function TimeLog({
  timeLog,
  onCreate,
  onUpdate,
  onDelete,
  buildTime
}) {
  const currentTime = dayjs();
  const [ticket, setTicket] = useState(timeLog.ticket || "");
  const [startTime, setStartTime] = useState(timeLog.startTime);
  const [endTime, setEndTime] = useState(timeLog.endTime);
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
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showConfirmUpdateModal, setShowConfirmUpdateModal] = useState(false);

  const [startTimeError, setStartTimeError] = useState(false);
  const [endTimeError, setEndTimeError] = useState(false);

  const timeLogRef = useRef(null);
  const {addAlert} = useAppContext();
  useEffect(() => {
    initializeState();
  }, [timeLog]);

  function initializeState() {
    setTicket(timeLog.ticket || "");
    setStartTime(timeLog.startTime);
    setEndTime(timeLog.endTime);
    setDescription(timeLog.description || "");
    setTotalTime(timeLog.totalTime || "");
  }

  const handleUpdateTimeLog = async (body) => {
    if (!body.validated && !validateUpdateRequest(body)) {
      return;
    }
    setIsLoading(true);
    setIsEditing(false);
    try {
      await onUpdate({
        ...body,
        startTime: dateTimeService.getFormattedDateTime(body.startTime),
        endTime: dateTimeService.getFormattedDateTime(body.endTime)
      });
    } catch (error) {
      resetChanges();
    } finally {
      setIsLoading(false);
    }
  };

  function resetChanges() {
    initializeState();
    setIsEditing(false);
  }

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [timeLogRef.current, startTime, endTime, ticket, description, timeLog]);

  function handleClickOutside(event) {
    if (timeLogRef.current && !timeLogRef.current.contains(event.target)) {
      setIsEditing(false);
      if (isModified) {
        handleUpdateTimeLog({
          id: timeLog.id,
          ticket,
          startTime,
          endTime,
          description
        });
      }
    }
  }

  const isModified = useMemo(() => {
    return (
      (ticket || "") !== (timeLog.ticket || "") ||
      (description || "") !== (timeLog.description || "") ||
      !isSameDate(startTime, timeLog.startTime) ||
      !isSameDate(endTime, timeLog.endTime)
    );
  }, [ticket, description, startTime, endTime, timeLog]);

  function isSameDate(date1, date2) {
    if (!date1 && !date2) return true;
    if (!date1 || !date2) return false;
    return date1.isSame(date2, "second");
  }

  useEffect(() => {
    if (isEditing && editedField) {
      timeLogRef.current.querySelector(`[name="${editedField}"]`).focus();
    }
  }, [isEditing, editedField]);

  const validateUpdateRequest = (body) => {
    const startTime = body.startTime
    const endTime = body.endTime
    const alerts = [];
    if (startTime && endTime && Math.abs(startTime.diff(endTime, "minute")) >= 1440) {
      alerts.push({
        text: "Time log can not durate more than 24 hours. Set end time manually.",
        type: "error"
      });
    }
    if (!isTicketFieldValid) {
      alerts.push({
        text: "Invalid ticket number",
        type: "error"
      });
    }
    if(alerts.length === 0 && startTime && endTime && dateTimeService.compareTimes(startTime, endTime) > 0) {
      setShowConfirmUpdateModal(true);
    }
    else if (alerts.length > 0) {
      alerts.forEach(alert => addAlert(alert));
      resetChanges();
      return false;
    } else {
      return true;
    }
  }

  const jiraIssuePattern = /^[A-Z]{2,}-\d+/;
  const isTicketFieldValid = ticket ? ticket?.match(jiraIssuePattern) : true;

  function getEditableFields() {
    return <>
      {createTimeField({
        name: "startTime",
        label: "Start",
        value: startTime,
        setValue: setStartTime,
        error: startTimeError,
        setError: setStartTimeError
      })}
      {createTimeField({
        name: "endTime",
        label: "End",
        value: endTime,
        setValue: setEndTime,
        error: endTimeError,
        setError: setEndTimeError
      })}
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
    </>;
  }

  const createTimeField = ({name, label, value, setValue, error, setError}) => {
    return (
      <div className="mr-4 my-2">
        <TimeField
          name={name}
          error={error}
          className="w-20"
          label={label}
          size="small"
          value={value}
          onChange={(timeToSet) => {
            validateTimeFields(timeToSet, setError);
            if (timeToSet === null) {
              setValue(null);
            } else {
              const newValue = name === 'startTime'
                ? buildTime.startTime(timeLog.date, timeToSet)
                : buildTime.endTime(timeLog.date, startTime, timeToSet);
              setValue(newValue);
            }
          }}
          format="HH:mm"
        />
      </div>
    );
  };
  function validateTimeFields(newTime, setError) {
    if (newTime === null || (newTime.isValid && newTime.isValid())) {
      setError(false);
    } else {
      setError(true);
    }
  }

  function getNonEditableFields() {
    return <>
      {(startTime || endTime) &&
        <div
          className="mr-4 my-2 hover:bg-blue-100"
          onClick={() => {
            setIsEditing(true);
            setEditedField("startTime");
          }}
        >
          <Typography className={`${startTime ? "font-bold" : "text-xs leading-6"}`}>
            {startTime ? dateTimeService.getFormattedTime(startTime) : "____"}
          </Typography>
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
          {(startTime || endTime) &&
            <Divider className="bg-gray-500 mr-4" orientation="vertical" variant="middle" sx={{borderRightWidth: 2}} flexItem />}
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

  const diffInMinutes = currentTime.diff(dayjs(timeLog.startTime), "minute");
  const statusConfig = {
    Done: {
      label: totalTime,
      action: (isHovered || isEditing) && (
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
      label: diffInMinutes >= 0 && diffInMinutes < 1440
        ? `${currentTime.diff(timeLog.startTime, "hour")}h ${diffInMinutes % 60}m`
        : null,
      action: (isHovered || isEditing) && (
        <Tooltip title="stop">
          <IconButton
            onClick={() => {
              setIsLoading(true);
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime,
                endTime: currentTime,
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
      action: (isHovered || isEditing) && (
        <Tooltip title="start">
          <IconButton
            onClick={() => {
              setIsLoading(true);
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime: currentTime,
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
          <ConfirmationModal
            open={showConfirmUpdateModal}
            type="info"
            actionText="OK"
            onConfirm={() => {
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime,
                endTime,
                description,
                validated: true
              });
            }}
            onClose={() => {
              resetChanges();
              setShowConfirmUpdateModal(false);
              setIsHovered(false);
            }}
          >
            Are you sure you want to set end of time to next day?
          </ConfirmationModal>

          {statusConfig[status].label ? <Chip
            label={statusConfig[status].label}
            color="primary"
            variant="outlined"
            size="small"
            className="shadow-md mr-2 my-2"
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
                      onClick={() => {
                        handleUpdateTimeLog({
                          id: timeLog.id,
                          ticket,
                          startTime,
                          endTime,
                          description
                        });

                      }}
                      className="mr-0"
                      color="success"
                      disabled={(startTimeError || endTimeError) || !isTicketFieldValid}
                    >
                      <SaveOutlinedIcon fontSize="small" />
                    </IconButton>
                  </span>
                </Tooltip>
              </div>
            )}

            {statusConfig[status] ? statusConfig[status].action : null}
            {(isHovered || isEditing) &&
              <>
                <Tooltip title="Delete">
                  <IconButton
                    color="error"
                    onClick={() => setShowDeleteModal(true)}
                  >
                    <DeleteOutlineOutlinedIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <ConfirmationModal
                  open={showDeleteModal}
                  type="error"
                  actionText="Delete"
                  onConfirm={() => {
                    setIsLoading(true);
                    onDelete(timeLog.id);
                    setIsLoading(false);
                  }}
                  onClose={() => {
                    setShowDeleteModal(false);
                    setIsHovered(false);
                  }}
                >
                  Are you sure you want to delete this time log?
                </ConfirmationModal>
              </>
            }
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
      {isLoading && <LinearProgress />}
    </div>
  );
}