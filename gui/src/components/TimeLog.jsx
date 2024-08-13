import {Chip, Icon, IconButton, LinearProgress, TextField, Tooltip, Typography} from "@mui/material";
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
import Button from "@mui/material/Button";
import {TiArrowForward} from "react-icons/ti";
import ArrowBackIosIcon from "@mui/icons-material/ArrowBackIos.js";
import ArrowForwardIosIcon from "@mui/icons-material/ArrowForwardIos.js";
import JoinFullIcon from '@mui/icons-material/JoinFull';
import Description from "./Description.jsx";
import {deepOrange} from "@mui/material/colors";

export default function TimeLog({
  timeLog,
  onCreate,
  onUpdate,
  onDelete,
  groupByDescription,
  changeDate,
  hovered,
  setGroupDescription
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

  const isTimeLogInNextDay = dateTimeService.isTimeLogInNextDay(startTime, endTime);

  const updateTimeLog = async (body) => {
    if (!isTicketFieldValid) {
      addAlert({
        text: "Invalid ticket number",
        type: "error"
      });
      resetChanges();
    } else {
      setIsEditing(false);
      const date = dateTimeService.getFormattedDate(isTimeLogInNextDay.startTime ? timeLog.date.add(1, "day") : timeLog.date);
      const ticket = body.ticket ? body.ticket.toUpperCase() : null;
      await onUpdate({
        ...body,
        date,
        ticket,
        startTime: dateTimeService.getFormattedDateTime(body.startTime),
        endTime: dateTimeService.getFormattedDateTime(body.endTime)
      });
    }
  };

  const jiraIssuePattern = /^[A-Za-z]{2,}-\d+/;
  const isTicketFieldValid = ticket ? ticket?.match(jiraIssuePattern) : true;

  function resetChanges() {
    initializeState();
    setIsEditing(false);
  }

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
  const {execute: handleChangeDate, isExecuting: isChangingDate} = useAsyncCall({
    fn: changeDate
  })

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
        });
      }
    }
  }

  const isModified = useMemo(() => {
    return (
      (ticket || "") !== (timeLog.ticket || "") ||
      !dateTimeService.isSameDateTime(startTime, timeLog.startTime) ||
      !dateTimeService.isSameDateTime(endTime, timeLog.endTime)
    );
  }, [ticket, startTime, endTime, timeLog]);

  useEffect(() => {
    if (isEditing && editedField) {
      timeLogRef.current.querySelector(`[name="${editedField}"]`).focus();
    }
  }, [isEditing, editedField]);

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
          inputProps={{style: {textTransform: "uppercase"}}}
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
          className="flex mr-4 my-1 hover:bg-blue-100"
          onClick={() => {
            setIsEditing(true);
            setEditedField("startTime");
          }}
        >
          {startTime && isTimeLogInNextDay.startTime &&
            <Tooltip className="flex items-center mr-1" title="next day">
              <Icon fontSize="small">
                <TiArrowForward />
              </Icon>
            </Tooltip>
          }
          <Typography className={`text-sm ${startTime ? "font-bold" : "text-xs leading-6"}`}>
            {startTime ? dateTimeService.getFormattedTime(startTime) : "____"}
          </Typography>
        </div>
      }
      {endTime && (
        <>
          -
          <div
            className="flex mx-4 my-1 hover:bg-blue-100"
            onClick={() => {
              setIsEditing(true);
              setEditedField("endTime");
            }}
          >
            {endTime && isTimeLogInNextDay.endTime > 0 &&
              <Tooltip className="flex items-center" title="next day">
                <Icon fontSize="small">
                  <TiArrowForward/>
                </Icon>
              </Tooltip>
            }
            <Typography className="mx-1 font-bold text-sm">{dateTimeService.getFormattedTime(endTime)}</Typography>
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

  const getDateChanger = () => {
    return (
      <div className="mr-2">
        <Tooltip title="move to previous day">
          <Button
            className="py-0 pl-1.5 pr-0 min-w-0"
            size="small"
            onClick={() => {
              handleChangeDate({id: timeLog.id, isNext: false})
            }}
            disabled={isChangingDate}
          >
            <ArrowBackIosIcon fontSize="small" />
          </Button>
        </Tooltip>
        <Tooltip title="move to next day">
          <Button
            className="py-0 pl-1.5 pr-0 min-w-0"
            size="small"
            onClick={() => {
              handleChangeDate({id: timeLog.id, isNext: true})
            }}
            disabled={isChangingDate}
          >
            <ArrowForwardIosIcon fontSize="small" />
          </Button>
        </Tooltip>
      </div>
    )
  }

  const progressTime = status === "InProgress" ? dateTimeService.getDurationOfProgressTimeLog(timeLog.startTime) : null;
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
      label: progressTime,
      action: ((isHovered || isEditing) && progressTime)
      && (
        <Tooltip title="stop">
          <IconButton
            onClick={() => {
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime,
                endTime: currentTime,
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
      action: ((isHovered || isEditing) && dateTimeService.isSameDate(dayjs(timeLog.date), currentTime)) && (
        <Tooltip title="start">
          <IconButton
            onClick={() => {
              handleUpdateTimeLog({
                id: timeLog.id,
                ticket,
                startTime: currentTime,
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
      className={`py-1 px-4  ${status === "InProgress" ? "bg-blue-50" : ""} ${hovered ? "bg-blue-100" : ""}`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div ref={timeLogRef} className="flex justify-between">
        <div className="flex items-center">
          {isEditing ? getEditableFields() : getNonEditableFields()}

          {statusConfig[status].label ? <Chip
            label={statusConfig[status].label}
            color="primary"
            variant="outlined"
            size="small"
            className="shadow-md mr-2"
          /> : null}
          {timeLog.conflicted && (
            <Tooltip title="conflicted">
              <JoinFullIcon sx={{color: deepOrange[200]}} className="text-red" />
            </Tooltip>
          )}
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

            {isHovered && !isEditing && getDateChanger()}
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

      {!groupByDescription && <Description description={description} ids={[timeLog.id]} setGroupDescription={setGroupDescription}/>}
      {(isCreateLoading || isUpdateLoading || isDeleteLoading) && <LinearProgress />}
    </div>
  );
}