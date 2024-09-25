import {Icon, IconButton, LinearProgress, ListItemIcon, ListItemText, Menu, MenuItem, TextField, Tooltip, Typography} from "@mui/material";
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
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import Description from "./Description.jsx";
import {deepOrange} from "@mui/material/colors";
import DoneIcon from '@mui/icons-material/Done';
import SyncDisabledIcon from '@mui/icons-material/SyncDisabled';
import SyncIcon from '@mui/icons-material/Sync';
import Duration from "./Duration.jsx";
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';
import VerticalDivider from "./VerticalDivider.jsx";
import MoreVertIcon from '@mui/icons-material/MoreVert';

export default function TimeLog({
  timeLog,
  onCreate,
  onDivide,
  onUpdate,
  onDelete,
  groupByDescription,
  onWorklogCreate,
  changeDate,
  hovered,
  setGroupDescription,
  setHoveredProgressIntervalId,
  hoveredConflictedIds,
  setHoveredConflictedIds,
  onSync,
  isJiraEditMode
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
  const [menuEl, setMenuEl] = useState(null);

  const [moreActionsMenuEl, setMoreActionsMenuEl] = useState(null);

  useEffect(() => {
    if(!isHovered || !isEditing) {
      setMoreActionsMenuEl(null)
    }
  }, [isHovered, isEditing]);
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
  const {execute: handleDivideTimeLog, isExecuting: isDivideLoading} = useAsyncCall({
    fn: onDivide,
  })
  const {execute: handleUpdateTimeLog, isExecuting: isUpdateLoading} = useAsyncCall({
    fn: updateTimeLog,
    onError: resetChanges,
  })
  const {execute: handleDeleteTimeLog, isExecuting: isDeleteLoading} = useAsyncCall({
    fn: onDelete,
  })
  const {execute: handleCreateWorklog, isExecuting: isCreatingWorklogLoading} = useAsyncCall({
    fn: onWorklogCreate,
  })
  const {execute: handleChangeDate, isExecuting: isChangingDate} = useAsyncCall({
    fn: changeDate
  })
  const {execute: handleSync, isExecuting: isSyncing} = useAsyncCall({
    fn: onSync
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
            <Tooltip className="flex items-center mr-1" title="Next day">
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
              <Tooltip className="flex items-center" title="Next day">
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
            <VerticalDivider />
          }
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

  const progressTime = status === "InProgress"
    ? dateTimeService.formatDuration(dateTimeService.getDurationInMinutes(timeLog.startTime, null))
    : null;

  const statusConfig = {
    Done: {
      label: totalTime,
      condition: true,
      icon: <KeyboardTabOutlinedIcon color="primary" fontSize="small"/>,
      onClick: () => handleCreateTimeLog({
        ticket,
        startTime: dateTimeService.getFormattedDateTime(currentTime),
        description
      }),
      text: "Continue"
    },
    InProgress: {
      label: progressTime,
      condition: progressTime,
      icon: <StopCircleOutlinedIcon color="warning" fontSize="small"/>,
      onClick: () => handleUpdateTimeLog({
          id: timeLog.id,
          ticket,
          startTime,
          endTime: currentTime,
      }),
      text: "Stop"
    },
    Pending: {
      label: 'Pending',
      condition: dateTimeService.isSameDate(dayjs(timeLog.date), currentTime),
      icon: <StartOutlinedIcon color="primary" fontSize="small"/>,
      onClick: () => handleUpdateTimeLog({
          id: timeLog.id,
          ticket,
          startTime: currentTime,
      }),
      text: "Start"
  }};

  return (
    <div
      className={`px-4  
      ${status === "InProgress" ? "bg-blue-50" : ""} 
      ${hovered ? "bg-blue-100" : ""} 
      ${hoveredConflictedIds?.includes(timeLog.id) ? "bg-rose-100" : ""}`}
      onMouseEnter={() => {
        setIsHovered(true);
        setHoveredProgressIntervalId && setHoveredProgressIntervalId(timeLog.id)
      }}
      onMouseLeave={() => {
        setIsHovered(false);
        setHoveredProgressIntervalId && setHoveredProgressIntervalId(null)
      }}
    >
      <div ref={timeLogRef} className="flex justify-between">
        <div className="flex items-center">
          {isEditing ? getEditableFields() : getNonEditableFields()}

          {statusConfig[status].label ? <Duration duration={statusConfig[status].label} /> : null}

          {timeLog.synced && timeLog.startTime && timeLog.endTime
            ? (
              <Tooltip title="Synchronized">
                <DoneIcon color="success" />
              </Tooltip>
            )
            : (
              <Tooltip title="Not synchronized">
                <SyncDisabledIcon color="error" />
              </Tooltip>
            )
          }

          {timeLog.isConflicted && (
            <Tooltip
              title="Conflicted"
              onMouseEnter={() => setHoveredConflictedIds(timeLog.conflictedIds)}
              onMouseLeave={() => setHoveredConflictedIds([])}
            >
              <WarningAmberIcon sx={{color: deepOrange[200]}} className="text-red" />
            </Tooltip>
          )}

          {timeLog.endTime?.isAfter(isTimeLogInNextDay.startTime
              ? dateTimeService.getStartOfDay(timeLog.startTime)
              : dateTimeService.getStartOfDay(timeLog.startTime.add(1, "day"))) &&
            <div>
              <Button onClick={(event) => setMenuEl(event.currentTarget)}>
                <Tooltip title="Timelog continues tomorrow">
                  <WarningAmberIcon sx={{color: deepOrange[200]}} className="text-red" />
                </Tooltip>
              </Button>
              <Menu
                anchorEl={menuEl}
                open={!!menuEl}
                onClose={() => setMenuEl(null)}
              >
                <MenuItem className="py-0 px-2" onClick={() => handleDivideTimeLog(timeLog.id)}>Divide into two days</MenuItem>
                <Divider/>
                <MenuItem className="py-0 px-2" onClick={() => setMenuEl(null)}>Cancel</MenuItem>
              </Menu>
            </div>
          }
        </div>

        <div className="flex items-center">
          {(isEditing && !isJiraEditMode) && (
            <div>
              <Tooltip title="Reset">
                <IconButton onClick={() => resetChanges()} className="mr-1">
                  <BackspaceOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>

              <Tooltip title="Save">
                  <span>
                    <IconButton
                      onClick={() => {
                        handleUpdateTimeLog({
                          id: timeLog.id, ticket, startTime, endTime,
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

          {(isHovered && !isEditing) && <div>
            <Tooltip title="More">
              <IconButton
                onClick={(event) => setMoreActionsMenuEl(event.currentTarget)}
                color="primary"
                className="p-0"
              >
                <MoreVertIcon />
              </IconButton>
            </Tooltip>

            <Menu
              anchorEl={moreActionsMenuEl}
              open={!!moreActionsMenuEl}
              onClose={() => {
                setIsHovered(false);
              }}
            >
              <MenuItem onClick={() => setIsEditing(true)}>
                <ListItemIcon>
                  <EditOutlinedIcon color="success" fontSize="small" />
                </ListItemIcon>
                <ListItemText>
                  <Typography className="text-sm">Edit</Typography>
                </ListItemText>
              </MenuItem>

              {statusConfig[status].condition && (
                <MenuItem onClick={statusConfig[status].onClick}>
                  <ListItemIcon>
                    {statusConfig[status].icon}
                  </ListItemIcon>
                  <ListItemText>
                    <Typography className="text-sm">{statusConfig[status].text}</Typography>
                  </ListItemText>
                </MenuItem>
              )}

              {timeLog.ticket && (
                <MenuItem onClick={() => handleSync(timeLog.ticket)}>
                  <ListItemIcon>
                    <SyncIcon color="primary" fontSize="small" />
                  </ListItemIcon>
                  <ListItemText>
                    <Typography className="text-sm">Synchronize</Typography>
                  </ListItemText>
                </MenuItem>
              )}

              {(timeLog.ticket && timeLog.startTime && timeLog.endTime && !timeLog.synced) && (
                <MenuItem onClick={() => handleCreateWorklog({
                  ticket: timeLog.ticket,
                  date: dateTimeService.getFormattedDate(timeLog.date),
                  startTime: dateTimeService.getFormattedDateTime(timeLog.startTime),
                  endTime: dateTimeService.getFormattedDateTime(timeLog.endTime),
                  description: timeLog.description
                })}>
                  <ListItemIcon>
                    <KeyboardDoubleArrowRightIcon color="primary" fontSize="small" />
                  </ListItemIcon>
                  <ListItemText>
                    <Typography className="text-sm">Save to worklogs</Typography>
                  </ListItemText>
                </MenuItem>
              )}

              <MenuItem onClick={() => {
                handleChangeDate({id: timeLog.id, isNext: false})
              }}>
                <ListItemIcon>
                  <ArrowBackIosIcon color="primary" fontSize="small" />
                </ListItemIcon>
                <ListItemText>
                  <Typography className="text-sm">To previous day</Typography>
                </ListItemText>
              </MenuItem>

              <MenuItem onClick={() => {
                handleChangeDate({id: timeLog.id, isNext: true})
              }}>
                <ListItemIcon>
                  <ArrowForwardIosIcon color="primary" fontSize="small" />
                </ListItemIcon>
                <ListItemText>
                  <Typography className="text-sm">To next day</Typography>
                </ListItemText>
              </MenuItem>

              <MenuItem onClick={() => setShowDeleteModal(true)}>
                <ListItemIcon>
                  <DeleteOutlineOutlinedIcon color="error" fontSize="small" />
                </ListItemIcon>
                <ListItemText>
                  <Typography className="text-sm">Delete</Typography>
                </ListItemText>
              </MenuItem>
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
            </Menu>

          </div>
          }
        </div>
      </div>

      {!groupByDescription &&
        <Description className="mb-1" description={description} ids={[timeLog.id]} setGroupDescription={setGroupDescription} />}
      {(isCreateLoading || isUpdateLoading || isDeleteLoading || isDivideLoading || isSyncing || isCreatingWorklogLoading || isChangingDate) &&
        <LinearProgress />}
    </div>
  );
}