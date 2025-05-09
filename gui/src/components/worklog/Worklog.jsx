import dateTimeService from "../../service/dateTimeService.js";
import Duration from "../general/Duration.jsx";
import {IconButton, LinearProgress, Tooltip} from "@mui/material";
import DeleteOutlineOutlinedIcon from "@mui/icons-material/DeleteOutlineOutlined.js";
import ConfirmationModal from "../general/ConfirmationModal.jsx";
import {useEffect, useRef, useState} from "react";
import KeyboardDoubleArrowLeftIcon from '@mui/icons-material/KeyboardDoubleArrowLeft';
import useAsyncCall from "../../hooks/useAsyncCall.js";
import dayjs from "dayjs";
import useAppContext from "../../context/useAppContext.js";
import Brightness1Icon from "@mui/icons-material/Brightness1.js";
import {syncStatus} from "../../consts/syncStatus.js";
import TimeLogSyncStatusIcon from "../timeLog/TimeLogSyncStatusIcon.jsx";
import TimeLogNonEditableFields from "../timeLog/TimeLogNonEditableFields.jsx";
import WorklogConnectors from "./WorklogConnectors.jsx";
import timeLogService from "../../service/timeLogService.js";
import {isJiraSyncingEnabled} from "../../config/config.js";

export default function Worklog({worklog, onTimeLogCreate, onDelete, isJiraEditMode}) {
  const worklogRef = useRef(null);
  const {externalTimeLogRefs, setExternalTimeLogRefs, timeLogRefs} = useAppContext();

  useEffect(() => {
    if (worklogRef.current && isJiraEditMode) {
      setExternalTimeLogRefs((prev) => {
        const existingIndex = prev.findIndex(({externalTimeLog: {id}}) => id === worklog.id);
        if (existingIndex !== -1) {
          const updatedRefs = [...prev];
          updatedRefs[existingIndex] = {externalTimeLog: worklog, ref: worklogRef};
          return updatedRefs;
        } else {
          return [...prev, {externalTimeLog: worklog, ref: worklogRef}];
        }
      });
    }
  }, [worklogRef])

  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const startTime = dayjs(worklog.startTime, "HH:mm");
  const endTime = startTime.add(worklog.timeSpentSeconds, "second")
  const isTimeLogInNextDay = timeLogService.getIsTimeLogInNextDayInfo(startTime, endTime);

  const {execute: handleDeleteWorklog, isExecuting: isDeleteLoading} = useAsyncCall({
    fn: onDelete,
  })

  const {execute: handleCreateTimeLogFromWorklog, isExecuting: isCreateLoading} = useAsyncCall({
    fn: onTimeLogCreate,
  })

  return (
    <div className="mb-2 px-4 py-1 shadow-md rounded-md bg-gray-50"
         ref={worklogRef}
         onMouseEnter={() => setIsHovered(true)}
         onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between">
        <div className="flex items-center pt-1.5 my-1">
          {isJiraSyncingEnabled && isJiraEditMode && worklog.jiraSyncInfo.status !== syncStatus.NOT_SYNCED && (
            <>
              <Brightness1Icon className="mr-2" sx={{color: worklog.jiraSyncInfo.color}} />
              {isHovered && (
                <WorklogConnectors
                  isHovered={isHovered}
                  sourceRefs={timeLogRefs}
                  targetRefs={externalTimeLogRefs}
                  sourceItem={worklog}
                />
              )}
            </>
          )}

          <TimeLogNonEditableFields
            startTime={startTime}
            endTime={endTime}
            ticket={worklog.ticket}
            isTimeLogInNextDay={isTimeLogInNextDay}
          />
          <Duration className="mx-2" duration={dateTimeService.formatMinutesToHM(worklog.timeSpentSeconds / 60)} />
          {isJiraSyncingEnabled && <TimeLogSyncStatusIcon serviceName="Jira" status={worklog.jiraSyncInfo.status} />}
        </div>
        <div>
          {isHovered && (
            <>
              {worklog.jiraSyncInfo.status === syncStatus.NOT_SYNCED && <Tooltip title="Save to my time logs">
                <IconButton
                  onClick={() => handleCreateTimeLogFromWorklog({
                    ticket: worklog.ticket,
                    date: worklog.date,
                    startTime: worklog.startTime,
                    description: worklog.comment,
                    timeSpentSeconds: worklog.timeSpentSeconds
                  })}
                  variant="outlined"
                  color="primary"
                  className="mr-2 p-0"
                >
                  <KeyboardDoubleArrowLeftIcon />
                </IconButton>
              </Tooltip>
              }
              <Tooltip title="Delete">
                <IconButton
                  className="mr-2 p-0"
                  color="error"
                  onClick={() => setShowDeleteModal(true)}
                >
                  <DeleteOutlineOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </>
          )
          }
          <ConfirmationModal
            open={showDeleteModal}
            type="error"
            actionText="Delete"
            onConfirm={() => handleDeleteWorklog({issueKey: worklog.ticket, id: worklog.id})}
            onClose={() => {
              setShowDeleteModal(false);
              setIsHovered(false);
            }}
          >
            Are you sure you want to delete this worklog?
          </ConfirmationModal>

        </div>
      </div>

      <div className="flex items-center">
        {worklog.comment}
      </div>

      {isDeleteLoading || isCreateLoading && <LinearProgress />}
    </div>
  )
}