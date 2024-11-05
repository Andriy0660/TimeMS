import {IconButton, LinearProgress, Tooltip} from "@mui/material";
import {useEffect, useMemo, useRef, useState} from "react";
import useAppContext from "../../context/useAppContext.js";
import dateTimeService from "../../service/dateTimeService.js";
import ConfirmationModal from "../general/ConfirmationModal.jsx";
import useAsyncCall from "../../hooks/useAsyncCall.js";
import TimeLogDescription from "./TimeLogDescription.jsx";
import Duration from "../general/Duration.jsx";
import MoreVertIcon from '@mui/icons-material/MoreVert';
import classNames from "classnames";
import Brightness1Icon from "@mui/icons-material/Brightness1";
import {syncStatus} from "../../consts/syncStatus.js";
import TimeLogStatusIcons from "./TimeLogStatusIcons.jsx";
import TimeLogEditableFields from "./TimeLogEditableFields.jsx";
import TimeLogNonEditableFields from "./TimeLogNonEditableFields.jsx";
import TimeLogMoreActionsMenu from "./TimeLogMoreActionsMenu.jsx";
import WorklogConnectors from "../worklog/WorklogConnectors.jsx";
import SaveButton from "./SaveButton.jsx";
import ResetButton from "./ResetButton.jsx";
import {timeLogStatus} from "../../consts/timeLogStatus.js";
import timeLogService from "../../service/timeLogService.js";
import {
  externalServiceIncludeDescription,
  externalTimeLogTimeCf,
  isExternalServiceSyncingEnabled,
  isJiraSyncingEnabled
} from "../../config/config.js";
import TimeLogLabelList from "./TimeLogLabelList.jsx";
import TimeLogLabelEditor from "./TimeLogLabelEditor.jsx";
import ExternalTimeLogConnectors from "../externalTimeLog/ExternalTimeLogConnectors.jsx";

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
  onSyncForIssue,
  isInEditMode,
  isJiraEditMode,
  isExternalServiceEditMode,
  onExternalTimeLogCreate,
  onSyncIntoExternalService
}) {
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
  const {addAlert, externalTimeLogRefs, timeLogRefs, setTimeLogRefs} = useAppContext();

  const timeLogRef = useRef(null);
  const timeLogUpperPartRef = useRef(null);
  const [moreActionsMenuEl, setMoreActionsMenuEl] = useState(null);

  const [labels, setLabels] = useState(timeLog.labels || [])
  const [isLabelAdding, setIsLabelAdding] = useState(false);

  useEffect(() => {
    if (timeLogRef.current && isInEditMode) {
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
    setLabels(timeLog.labels || []);
  }

  const isTimeLogInNextDay = timeLogService.getIsTimeLogInNextDayInfo(startTime, endTime);

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
    fn: onSyncForIssue
  })
  const {execute: handleCreateExternalTimeLog, isExecuting: isCreatingExternalTimeLogLoading} = useAsyncCall({
    fn: onExternalTimeLogCreate,
  })
  const {execute: handleSyncIntoExternalService, isExecuting: isSyncingIntoExternalService} = useAsyncCall({
    fn: onSyncIntoExternalService,
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
      setIsLabelAdding(false);
      if (isModified) {
        handleUpdateTimeLog({
          id: timeLog.id,
          ticket,
          startTime,
          endTime,
          labels
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
      <div ref={timeLogUpperPartRef}>
        <div className="my-1 flex justify-between items-center" >
          <div
            className={classNames("pt-1.5 overflow-x-hidden flex items-center", {
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

            <Duration className="mr-2" duration={timeLog.totalTime ? timeLog.totalTime : timeLogStatus.PENDING} />
            {isExternalServiceSyncingEnabled && isExternalServiceEditMode && <Tooltip title="External Service Time">
              <span>
                <Duration color="green"
                        duration={dateTimeService.formatMinutesToHM(
                          Math.round(timeLog.endTime.diff(timeLog.startTime, "minute") / externalTimeLogTimeCf))} />
              </span>
            </Tooltip>
            }

            <TimeLogStatusIcons
              isConflicted={timeLog.isConflicted}
              isContinueUntilTomorrow={isContinueUntilTomorrow}
              jiraSyncStatus={timeLog.jiraSyncInfo.status}
              externalTimeLogSyncStatus={timeLog.externalServiceSyncInfo.status}
            />
            {labels.length < 4 && !isInEditMode && (
              <TimeLogLabelList className="ml-2" labels={labels} timeLog={timeLog} onUpdate={handleUpdateTimeLog} />
            )}
            {!isInEditMode && <TimeLogLabelEditor
              className="ml-2"
              timeLog={timeLog}
              isLabelAdding={isLabelAdding}
              setIsLabelAdding={setIsLabelAdding}
              handleUpdateTimeLog={handleUpdateTimeLog}
              isHovered={isHovered}/>
            }

          </div>

          <div className="flex items-center flex-nowrap">
            {(isEditing && !isInEditMode) && (
              <>
                <ResetButton onReset={resetChanges} />
                <SaveButton
                  onSave={() => handleUpdateTimeLog({id: timeLog.id, ticket, startTime, endTime, labels})}
                  className="mr-2 p-0"
                  disabled={startTimeError || endTimeError || !isTicketFieldValid}
                />
              </>
            )}

            {(isHovered && !isEditing) && (
              <div onClick={() => handleCloseMoreActionsMenu()}>
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

                <TimeLogMoreActionsMenu
                  moreActionsMenuEl={moreActionsMenuEl}
                  handleCloseMoreActionsMenu={handleCloseMoreActionsMenu}
                  timeLog={timeLog}
                  setIsEditing={setIsEditing}
                  isContinueUntilTomorrow={isContinueUntilTomorrow}

                  isJiraEditMode={isJiraEditMode}
                  isExternalServiceEditMode={isExternalServiceEditMode}

                  handleCreateTimeLog={handleCreateTimeLog}
                  handleUpdateTimeLog={handleUpdateTimeLog}
                  setShowDeleteModal={setShowDeleteModal}
                  handleDivideTimeLog={handleDivideTimeLog}
                  handleChangeDate={handleChangeDate}

                  handleCreateWorklog={handleCreateWorklog}
                  handleSyncForTicket={handleSyncForTicket}
                  handleSyncFromJira={handleSyncFromJira}
                  handleSyncIntoJira={handleSyncIntoJira}

                  handleCreateExternalTimeLog={handleCreateExternalTimeLog}
                  handleSyncIntoExternalService={handleSyncIntoExternalService}
                />
              </div>
            )}
            {isJiraSyncingEnabled && isJiraEditMode && timeLog.jiraSyncInfo.status !== syncStatus.NOT_SYNCED && (
              <>
                <Brightness1Icon sx={{color: timeLog.jiraSyncInfo.color}} />
                {isHovered && (
                  <WorklogConnectors
                    isHovered={isHovered}
                    sourceRefs={timeLogRefs}
                    targetRefs={externalTimeLogRefs}
                    sourceItem={timeLog}
                  />
                )}
              </>
            )}

            {isExternalServiceSyncingEnabled && isExternalServiceEditMode && externalServiceIncludeDescription && timeLog.externalServiceSyncInfo.status !== syncStatus.NOT_SYNCED && (
              <>
                <Brightness1Icon sx={{color: timeLog.externalServiceSyncInfo.color}} />
                {isHovered && (
                  <ExternalTimeLogConnectors
                    isHovered={isHovered}
                    sourceRefs={timeLogRefs}
                    targetRefs={externalTimeLogRefs}
                    sourceItem={timeLog}
                  />
                )}
              </>
            )}
          </div>
        </div>
        {(labels.length >= 4 || isInEditMode) && (
          <TimeLogLabelList labels={labels} timeLog={timeLog} onUpdate={handleUpdateTimeLog} wrap/>
        )}
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
        <TimeLogDescription className="w-fit" description={description} ids={[timeLog.id]} setGroupDescription={setGroupDescription} />}
      {(isCreateLoading || isUpdateLoading || isDeleteLoading || isDivideLoading || isCreatingWorklogLoading || isChangingDate
          || isSyncingIntoJira || isSyncingFromJira || isSyncing
          || isCreatingExternalTimeLogLoading || isSyncingIntoExternalService) &&
        <LinearProgress />}
    </div>
  );
}