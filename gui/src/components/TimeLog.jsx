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
import dateTimeService from "../service/dateTimeService.js";
import ConfirmationModal from "./ConfirmationModal.jsx";
import useAsyncCall from "../hooks/useAsyncCall.js";

export default function TimeLog({
  timeLog,
  onCreate,
  onUpdate,
  onDelete,
  groupByDescription
}) {
  const currentTime = dayjs();
  const [ticket, setTicket] = useState(timeLog.ticket || "");
  const [startTime, setStartTime] = useState(timeLog.startTime);
  const [endTime, setEndTime] = useState(timeLog.endTime);
  const [description, setDescription] = useState(timeLog.description || "");
  const [totalTime, setTotalTime] = useState(timeLog.totalTime);

  const status = timeLog.status

  const [isEditing, setIsEditing] = useState(false);
  const [editedField, setEditedField] = useState(null);
  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [confirmUpdateFunction, setConfirmUpdateFunction] = useState(null);
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

  const updateTimeLog = async (body) => {
    const validation = validateUpdateRequest(body);
    if (!validation.valid) {
      validation.alerts.forEach(alert => addAlert(alert));
      resetChanges();
    } else if (validation.requiresConfirmation) {
      setConfirmUpdateFunction(() => () => handleUpdateTimeLog({...body, validated: true}))
      setShowConfirmUpdateModal(true)
    } else {
      setIsEditing(false);
      await onUpdate({
        ...body,
        startTime: dateTimeService.getFormattedDateTime(body.startTime),
        endTime: dateTimeService.getFormattedDateTime(body.endTime)
      });
    }
  };

  const {execute: handleCreateTimeLog, isExecuting: isCreateLoading} = useAsyncCall({
    fn: onCreate,
  })
  const {execute: handleUpdateTimeLog, isExecuting: isUpdateLoading} = useAsyncCall({
    fn: updateTimeLog,
    onError: resetChanges,
  })
  const {execute: handleDeleteTimeLog, isExecuting: isDeleteLoading} = useAsyncCall({
    fn: onDelete,
  })

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
      !dateTimeService.isSameDate(startTime, timeLog.startTime) ||
      !dateTimeService.isSameDate(endTime, timeLog.endTime)
    );
  }, [ticket, description, startTime, endTime, timeLog]);

  useEffect(() => {
    if (isEditing && editedField) {
      timeLogRef.current.querySelector(`[name="${editedField}"]`).focus();
    }
  }, [isEditing, editedField]);

  const shouldDisplayConfirmUpdateModal = startTime && endTime && dateTimeService.compareTimes(startTime, endTime) > 0 &&
    !dateTimeService.isSameDate(endTime, timeLog.endTime);
  const validateUpdateRequest = (body) => {
    if(body.validated) {
      return {
        valid: true,
        requiresConfirmation: false
      };
    }
    const startTime = body.startTime;
    const endTime = body.endTime;
    const alerts = [];

    if (startTime && endTime && Math.abs(startTime.diff(endTime, "minute")) >= 1440) {
      alerts.push({
        text: "Time log can not last more than 24 hours. Set end time manually.",
        type: "error"
      });
    }

    if (!isTicketFieldValid) {
      alerts.push({
        text: "Invalid ticket number",
        type: "error"
      });
    }
    if (alerts.length === 0 && shouldDisplayConfirmUpdateModal) {
      return {
        valid: true,
        requiresConfirmation: true
      };
    }

    if (alerts.length > 0) {
      return {
        valid: false,
        alerts
      };
    }

    return {
      valid: true,
      requiresConfirmation: false
    };
  };


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
            } else if(timeToSet.isValid()){
                const newValue = name === 'startTime'
                  ? dateTimeService.buildStartTime(timeLog.date, timeToSet)
                  : dateTimeService.buildEndTime(timeLog.date, startTime, timeToSet);
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
          className="mr-4 my-1 hover:bg-blue-100"
          onClick={() => {
            setIsEditing(true);
            setEditedField("startTime");
          }}
        >
          <Typography className={`text-sm ${startTime ? "font-bold" : "text-xs leading-6"}`}>
            {startTime ? dateTimeService.getFormattedTime(startTime) : "____"}
          </Typography>
        </div>
      }
      {endTime && (
        <>
          -
          <div
            className="mx-4 my-1 hover:bg-blue-100"
            onClick={() => {
              setIsEditing(true);
              setEditedField("endTime");
            }}
          >
            <Typography className="font-bold text-sm">{dateTimeService.getFormattedTime(endTime)}</Typography>
          </div>
        </>
      )}

      {ticket && (
        <>
          {(startTime || endTime) &&
            <Divider className="bg-gray-500 my-0.5 mr-4" orientation="vertical" variant="middle" sx={{borderRightWidth: 2}} flexItem />}
          <div
            className="mr-4 hover:bg-blue-100"
            onClick={() => {
              setIsEditing(true);
              setEditedField("ticket");
            }}
          >
            <Typography className="font-bold my-1 text-sm">{ticket}</Typography>
          </div>
        </>
      )}

    </>;
  }

  const statusConfig = {
    Done: {
      label: totalTime,
      action: (isHovered || isEditing) && (
        <Tooltip title="continue">
          <IconButton
            onClick={() => handleCreateTimeLog(
              {ticket, startTime: dateTimeService.getFormattedDateTime(currentTime), description})}
            variant="outlined"
            color="primary"
            className="mr-2 p-0"
          >
            <KeyboardTabOutlinedIcon />
          </IconButton>
        </Tooltip>
      ),
    },
    InProgress: {
      label: dateTimeService.getDurationOfProgressTimeLog(timeLog.startTime),
      action: (isHovered || isEditing) && (
        <Tooltip title="stop">
          <IconButton
            onClick={() => {
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime,
                endTime: currentTime,
                description,
              });
            }}
            variant="outlined"
            color="warning"
            className="mr-2 p-0"
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
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime: currentTime,
                description,
              });
            }}
            variant="outlined"
            color="primary"
            className="mr-2 p-0"
          >
            <StartOutlinedIcon />
          </IconButton>
        </Tooltip>
      ),
    },
  };

  return (
    <div
      className={`py-1 px-4  ${status === "InProgress" ? "bg-blue-50" : ""}`}
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
            onConfirm={confirmUpdateFunction}
            onClose={() => {
              setShowConfirmUpdateModal(false);
              setIsHovered(false);
            }}
            onCancel={resetChanges}
          >
            Are you sure you want to set end of time to next day?
          </ConfirmationModal>

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
                  <IconButton className="mr-1">
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
                      className="mr-2 p-0"
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
                    className="mr-2 p-0"
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
                  onConfirm={() => handleDeleteTimeLog(timeLog.id)}
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
                  className="mr-2 p-0"
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

      {!groupByDescription && <div
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
      }
      {(isCreateLoading || isUpdateLoading || isDeleteLoading) && <LinearProgress />}
    </div>
  );
}