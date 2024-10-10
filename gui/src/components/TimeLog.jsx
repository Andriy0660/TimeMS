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
import SplitscreenIcon from '@mui/icons-material/Splitscreen';import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import ConfirmationModal from "./ConfirmationModal.jsx";
import useAsyncCall from "../hooks/useAsyncCall.js";
import TimeLogDescription from "./TimeLogDescription.jsx";
import Duration from "./Duration.jsx";
import VerticalDivider from "./VerticalDivider.jsx";
import MoreVertIcon from '@mui/icons-material/MoreVert';
import classNames from "classnames";
import Connector from "./Connector.jsx";
import Brightness1Icon from "@mui/icons-material/Brightness1";
import {syncStatus} from "../consts/syncStatus.js";
import SyncJiraButtons from "./SyncJiraButtons.jsx";
import TimeLogStatusIcons from "./TimeLogStatusIcons.jsx";
import TimeLogEditableFields from "./TimeLogEditableFields.jsx";
import TimeLogNonEditableFields from "./TimeLogNonEditableFields.jsx";

export default function TimeLog({
  timeLog,
  onCreate,
  onDivide,
  onUpdate,
  onDelete,
  groupByDescription,
  onWorklogCreate,
  onSyncIntoJira,
  onSyncFromJira,
  changeDate,
  hovered,
  setGroupDescription,
  setHoveredProgressIntervalId,
  hoveredConflictedIds,
  setHoveredConflictedIds,
  onSync,
  isJiraEditMode,
}) {
  const currentTime = dayjs();
  const [ticket, setTicket] = useState(timeLog.ticket || "");
  const [startTime, setStartTime] = useState(timeLog.startTime);
  const [endTime, setEndTime] = useState(timeLog.endTime);
  const [description, setDescription] = useState(timeLog.description || "");

  const [isEditing, setIsEditing] = useState(false);
  const [editedField, setEditedField] = useState(null);
  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [startTimeError, setStartTimeError] = useState(false);
  const [endTimeError, setEndTimeError] = useState(false);
  const {isJiraSyncingEnabled, addAlert, worklogRefs, timeLogRefs, setTimeLogRefs} = useAppContext();

  const timeLogRef = useRef(null);
  const timeLogUpperPartRef = useRef(null);
  const [moreActionsMenuEl, setMoreActionsMenuEl] = useState(null);

  useEffect(() => {
    if (timeLogRef.current && isJiraEditMode) {
      setTimeLogRefs((prev) => {
        const existingIndex = prev.findIndex(({timeLog: {id}}) => id === timeLog.id);
        if (existingIndex !== -1) {
          const updatedRefs = [...prev];
          updatedRefs[existingIndex] = {timeLog, ref: timeLogRef};
          return updatedRefs;
        } else {
          return [...prev, {timeLog, ref: timeLogRef}];
        }
      });
    }
  }, [timeLogRef])

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
  const {execute: handleChangeDate, isExecuting: isChangingDate} = useAsyncCall({
    fn: changeDate
  })
  const {execute: handleCreateWorklog, isExecuting: isCreatingWorklogLoading} = useAsyncCall({
    fn: onWorklogCreate,
  })
  const {execute: handleSyncIntoJira, isExecuting: isSyncingIntoJira} = useAsyncCall({
    fn: onSyncIntoJira,
  })
  const {execute: handleSyncFromJira, isExecuting: isSyncingFromJira} = useAsyncCall({
    fn: onSyncFromJira,
  })
  const {execute: handleSyncForTicket, isExecuting: isSyncing} = useAsyncCall({
    fn: onSync
  })

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [timeLogUpperPartRef.current, startTime, endTime, ticket, description, timeLog]);

  function handleClickOutside(event) {
    if (timeLogUpperPartRef.current && !timeLogUpperPartRef.current.contains(event.target)) {
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
      timeLogUpperPartRef.current.querySelector(`[name="${editedField}"]`).focus();
    }
  }, [isEditing, editedField]);

  useEffect(() => {
    if (!isHovered || !isEditing) {
      setMoreActionsMenuEl(null)
    }
  }, [isHovered, isEditing]);

  function handleCloseMoreActionsMenu() {
    setMoreActionsMenuEl(null);
    setHoveredProgressIntervalId?.(null);
    setIsHovered(false);
  }

  const isContinueUntilTomorrow = timeLog.endTime?.isAfter(isTimeLogInNextDay.startTime
    ? dateTimeService.getStartOfDay(timeLog.startTime)
    : dateTimeService.getStartOfDay(timeLog.startTime.add(1, "day")));

  return (
    <div
      ref={timeLogRef}
      onMouseEnter={() => {
        setIsHovered(true);
        setHoveredProgressIntervalId?.(timeLog.id);
        setHoveredConflictedIds?.(timeLog.conflictedIds)
      }}
      onMouseLeave={() => {
        setIsHovered(false);
        setHoveredProgressIntervalId?.(null);
        setHoveredConflictedIds?.([])
      }}
    >
      <div ref={timeLogUpperPartRef} className="flex justify-between">
        <div
          className={classNames("flex items-center mb-1", {
            "bg-blue-50": timeLog.status === "InProgress",
            "bg-blue-100": hovered,
            "bg-rose-100": hoveredConflictedIds?.includes(timeLog.id)
          })}>

          {isEditing && (
            <TimeLogEditableFields
              timeLog={timeLog}
              startTime={startTime}
              setStartTime={setStartTime}
              startTimeError={startTimeError}
              setStartTimeError={setStartTimeError}
              endTime={endTime}
              setEndTime={setEndTime}
              endTimeError={endTimeError}
              setEndTimeError={setEndTimeError}
              ticket={ticket}
              setTicket={setTicket}
              isTicketFieldValid={isTicketFieldValid}
            />
          )}
          {!isEditing && (
            <TimeLogNonEditableFields
              startTime={startTime}
              endTime={endTime}
              ticket={ticket}
              isTimeLogInNextDay={isTimeLogInNextDay}
              setIsEditing={setIsEditing}
              setEditedField={setEditedField}
            />
          )}

          <Duration className="mr-2" duration={timeLog.totalTime ? timeLog.totalTime : "Pending"} />
          <TimeLogStatusIcons
            isConflicted={timeLog.isConflicted}
            isContinueUntilTomorrow={isContinueUntilTomorrow}
            jiraSyncStatus={timeLog.jiraSyncInfo.status}
          />
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

          {(isHovered && !isEditing) && <div onClick={() => handleCloseMoreActionsMenu()}>
            <Tooltip title="More">
              <IconButton
                onClick={(event) => {
                  event.stopPropagation()
                  setMoreActionsMenuEl(event.currentTarget);
                }}
                color="primary"
                className="p-0"
              >
                <MoreVertIcon />
              </IconButton>
            </Tooltip>

            <Menu
              anchorEl={moreActionsMenuEl}
              open={!!moreActionsMenuEl}
              onClose={() => handleCloseMoreActionsMenu()}
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
              {isContinueUntilTomorrow &&
                <MenuItem onClick={() => handleDivideTimeLog(timeLog.id)}>
                  <ListItemIcon>
                    <SplitscreenIcon color="primary" fontSize="small" />
                  </ListItemIcon>
                  <ListItemText>
                    <Typography className="text-sm">Divide into two days</Typography>
                  </ListItemText>
                </MenuItem>
              }
              {isJiraSyncingEnabled && <SyncJiraButtons
                timeLog={timeLog}
                handleCreateWorklog={handleCreateWorklog}
                handleSyncForTicket={handleSyncForTicket}
                handleSyncFromJira={handleSyncFromJira}
                handleSyncIntoJira={handleSyncIntoJira}/>
              }

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

            </Menu>

          </div>
          }
          {isJiraSyncingEnabled && isJiraEditMode && timeLog.jiraSyncInfo.status !== syncStatus.NOT_SYNCED && (
            <>
              <Brightness1Icon sx={{color: timeLog.jiraSyncInfo.color}} />
              {isHovered && (
                <TimeLogWorklogConnectors
                  isHovered={isHovered}
                  sourceRefs={timeLogRefs}
                  targetRefs={worklogRefs}
                  sourceItem={timeLog}
                />
              )}
            </>
          )}
        </div>
      </div>
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

      {!groupByDescription &&
        <TimeLogDescription className="w-fit" description={description} ids={[timeLog.id]} isJiraEditMode={isJiraEditMode}
                            setGroupDescription={setGroupDescription} />}
      {(isCreateLoading || isUpdateLoading || isDeleteLoading || isDivideLoading || isCreatingWorklogLoading || isChangingDate
          || isSyncingIntoJira || isSyncingFromJira || isSyncing) &&
        <LinearProgress />}
    </div>
  );
}